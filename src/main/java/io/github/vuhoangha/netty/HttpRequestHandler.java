package io.github.vuhoangha.netty;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Slf4j
@ChannelHandler.Sharable
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    // chứa danh sách các api đã đăng ký
    private final Map<String, Map<HttpMethod, Route>> routes = new HashMap<>();

    // dùng để xác thực <session_token, api_key>
    private final TriConsumer<String, String, HttpData> authHandler;

    // dùng để tạo virtual thread
    private final ExecutorService vExec;

    // Object pool để lấy HttpData
    private final Consumer<HttpData> httpDataConsumer;

    // Object pool để trả HttpData
    private final Supplier<HttpData> httpDataSupplier;

    private final ObjectMapper objectMapper;

    private final ByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;


    public HttpRequestHandler(ExecutorService vExec, TriConsumer<String, String, HttpData> authHandler, Consumer<HttpData> httpDataConsumer, Supplier<HttpData> httpDataSupplier, ObjectMapper objectMapper) {
        this.vExec = vExec;
        this.authHandler = authHandler;
        this.httpDataConsumer = httpDataConsumer;
        this.httpDataSupplier = httpDataSupplier;
        this.objectMapper = objectMapper;
    }


    // đăng ký 1 api
    public void addRoute(String path, HttpMethod method, boolean requiresAuth, IHandler handler, Consumer<Object> bodyConsumer, Supplier<Object> bodySupplier) {
        routes.computeIfAbsent(path, k -> new HashMap<>()).put(method, new Route(requiresAuth, handler, bodyConsumer, bodySupplier));
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        // Tạo hoặc lấy ByteBuf từ pool
        ByteBuf content = allocator.buffer(request.content().readableBytes());
        content.writeBytes(request.content());

        vExec.submit(() -> {

            HttpData httpData = httpDataSupplier.get();
            Route route = null;

            try {
                // lấy path và query string
                parseUri(request.uri(), httpData.getUri());

                HttpMethod method = request.method();
                String path = httpData.getUri().getPath();

                // check api có tồn tại không
                if (!routes.containsKey(path) || !routes.get(path).containsKey(method)) {
                    sendError(httpData, route, ctx, HttpResponseStatus.NOT_FOUND);
                    return;
                }

                route = routes.get(path).get(method);

                // authen
                if (route.requiresAuth) {
                    String sessionToken = request.headers().get("session-token");
                    String apiKey = request.headers().get("api-key");
                    authHandler.accept(sessionToken, apiKey, httpData);
                    if (!httpData.getAuthData().isAuth) {
                        sendError(httpData, route, ctx, HttpResponseStatus.UNAUTHORIZED);
                        return;
                    }
                }

                // body
                if (route.bodyConsumer != null) {
                    parseBody(content, httpData, route.bodySupplier);
                }

                // phản hồi
                byte[] resByte = route.handler.process(httpData);
                sendResponse(httpData, route, ctx, resByte);

            } catch (NettyCustomException e) {
                // TODO tìm cách xử lý exception đoạn này
                sendError(httpData, route, ctx, HttpResponseStatus.BAD_REQUEST);
                log.error("HttpRequestHandler error", e);
            } catch (Exception e) {
                log.error("HttpRequestHandler error", e);
                sendError(httpData, route, ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
            } finally {
                // Giải phóng ByteBuf đã sao chép
                content.release();
            }
        });
    }


    private void sendResponse(HttpData httpData, Route route, ChannelHandlerContext ctx, byte[] jsonBytes) {
        //  trả data lại cho pool và clear
        returnToPoolAndClear(httpData, route);

        // ko phản hồi body gì
        if (jsonBytes == null) {
            responseOnlyStatus(ctx, HttpResponseStatus.OK);
            return;
        }

        // Tạo CompositeByteBuf và thêm dữ liệu JSON
        // TODO xem sử dụng pool ở đoạn này
        CompositeByteBuf content = PooledByteBufAllocator.DEFAULT.compositeBuffer();
        ByteBuf jsonBuf = PooledByteBufAllocator.DEFAULT.buffer(jsonBytes.length);
        jsonBuf.writeBytes(jsonBytes);
        content.addComponent(true, jsonBuf);

        // TODO xem tái sử dụng
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");

        // ghi và giải phóng tài nguyên --> Đóng kết nối
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }


    // TODO đoạn này tìm cách cache, json type
    private void sendError(HttpData httpData, Route route, ChannelHandlerContext ctx, HttpResponseStatus status) {
        //  trả data lại cho pool và clear
        returnToPoolAndClear(httpData, route);

        // gửi về cho client
        responseOnlyStatus(ctx, status);
    }


    private void returnToPoolAndClear(HttpData httpData, Route route) {
        // trả body lại pool
        if (route != null) {
            Object body = httpData.getBody();
            if (route.bodyConsumer != null && body != null) {
                route.bodyConsumer.accept(body);
            }
        }

        // trả httpData lại pool
        httpData.clear();
        httpDataConsumer.accept(httpData);
    }


    // TODO xem xét tái sử dụng đối tượng "DefaultFullHttpResponse" cho mỗi lỗi khác nhau
    private void responseOnlyStatus(ChannelHandlerContext ctx, HttpResponseStatus status) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status);
        response.headers().set(CONTENT_LENGTH, 0);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }


    // lấy thông tin path và query string của api
    private void parseUri(String uri, URI uriOut) {
        int queryStart = uri.indexOf("?");

        // lấy path
        if (queryStart == -1) {
            uriOut.setPath(uri);    // Không có tham số truy vấn, chỉ bao gồm path
        } else {
            uriOut.setPath(uri.substring(0, queryStart));   // Trả về phần trước dấu '?'
        }

        // lấy param
        Map<String, String> queryParams = uriOut.getQueries();
        if (queryStart != -1) {
            String query = uri.substring(queryStart + 1);
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length > 1) {
                    queryParams.put(keyValue[0], keyValue[1]);
                } else {
                    queryParams.put(keyValue[0], "");
                }
            }
        }
    }


    // TODO nhớ thử xem virtual thread có hoạt động chung cho bên auth và process ko nhé hay lại khác nhau
    private void parseBody(ByteBuf content, HttpData httpData, Supplier<Object> objectSupplier) {
        try {
            // ko có body bỏ qua
            if (!content.isReadable()) return;

            // TODO đoạn này tìm cách tái sử dụng sau
            byte[] bytes = new byte[content.readableBytes()];
            content.readBytes(bytes);
            JsonParser parser = objectMapper.getFactory().createParser(bytes);

            Object object = objectSupplier.get();
            objectMapper.readerForUpdating(object).readValue(parser);
            httpData.setBody(object);
        } catch (Exception ex) {
            log.error("HttpProcessHandler.parseBody error", ex);
            throw new NettyCustomException(ex);
        }
    }


    // chứa thông tin của 1 API
    private static class Route {

        // dùng để xử lý request
        private final IHandler handler;

        // có phải xác thực token/api-key không
        private final boolean requiresAuth;

        private final Consumer<Object> bodyConsumer;

        private final Supplier<Object> bodySupplier;

        Route(boolean requiresAuth, IHandler handler, Consumer<Object> bodyConsumer, Supplier<Object> bodySupplier) {
            this.handler = handler;
            this.bodyConsumer = bodyConsumer;
            this.bodySupplier = bodySupplier;
            this.requiresAuth = requiresAuth;
        }
    }
}
