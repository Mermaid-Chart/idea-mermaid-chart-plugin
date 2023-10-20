package co.tula.mermaidchart.services

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.QueryStringDecoder
import org.jetbrains.ide.RestService


class OAuthCallbackHandlerService: RestService() {
    override fun getServiceName(): String = "mermaid/oauth/callback"
    override fun execute(urlDecoder: QueryStringDecoder, request: FullHttpRequest, context: ChannelHandlerContext): String? {
        print(request.uri())
        sendOk(request, context)
        return null
    }

}