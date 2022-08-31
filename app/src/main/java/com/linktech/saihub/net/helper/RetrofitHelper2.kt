package com.linktech.saihub.net.helper

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.linktech.saihub.BuildConfig
import com.linktech.saihub.app.SaihubNetUrl.BASE_URL
import com.linktech.saihub.app.SaihubNetUrl.BLOCK_CHAIN_URL
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.entity.event.ErrorEvent
import com.linktech.saihub.net.ex.NullOnEmptyConverterFactory
import com.linktech.saihub.net.interceptor.CodeCheckInterceptor
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.IOException
import java.io.OutputStreamWriter
import java.io.StringReader
import java.io.Writer
import java.lang.reflect.Type
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit


class RetrofitHelper2() {

    private var mRetrofit: Retrofit? = null


    companion object {
        private var baseUrl: String? = null
        private const val DEFAULT_TIMEOUT_SECONDS = 60
        private const val DEFAULT_READ_TIMEOUT_SECONDS = 60
        private const val DEFAULT_WRITE_TIMEOUT_SECONDS = 60

        private fun getInstance(baseUrl: String): RetrofitHelper2 {
            this.baseUrl = baseUrl
            return SingletonHolder.INSTANCE
        }

        fun <T> getService(baseUrl: String, classz: Class<T>?): T {
            return getInstance(baseUrl).mRetrofit?.create(classz)!!
        }

        private fun bodyToString(request: RequestBody?): String? {
            return try {
                val buffer = Buffer()
                if (request != null) request.writeTo(buffer) else return ""
                buffer.readUtf8()
            } catch (e: IOException) {
                "did not work"
            }
        }

        private val MEDIA_TYPE = "application/json; charset=UTF-8".toMediaTypeOrNull()
        private val UTF_8 = Charset.forName("UTF-8")
    }


    init {

        val builder = OkHttpClient.Builder()
        builder.connectTimeout(DEFAULT_TIMEOUT_SECONDS.toLong(), TimeUnit.SECONDS)
            .readTimeout(DEFAULT_READ_TIMEOUT_SECONDS.toLong(), TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_WRITE_TIMEOUT_SECONDS.toLong(), TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(HeadInterceptor())
            .addInterceptor(CodeCheckInterceptor { status, message, url, method ->
                EventBus.getDefault().post(
                    ErrorEvent.getInstance(status, message, url, method)
                )
            })


        val interceptor = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            interceptor.level = HttpLoggingInterceptor.Level.BODY
        } else {
            interceptor.level = HttpLoggingInterceptor.Level.NONE
        }
        builder.addInterceptor(interceptor)
        if (mRetrofit == null) {
            val gson: Gson = GsonBuilder()
                .registerTypeAdapterFactory(NullStringEmptyTypeAdapterFactory)
                .setLenient()
                .create()
            mRetrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(NullOnEmptyConverterFactory())
                //添加Gson支持
                .addConverterFactory(MyGsonConverterFactory.create(gson))
//                .addCallAdapterFactory(RxJavaCallAdapterFactory.create()) //添加RxJava支持
                .client(builder.build()) //关联okhttp
                .build()
        }
    }

    //头部信息拦截器
    private class HeadInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request: Request = chain.request()
            val oldUrl = request.url
            val builder: Request.Builder = request.newBuilder()
            val headerValues = request.headers("url_name")
            return if (headerValues.isNotEmpty()) {
                builder.removeHeader("url_name")
                val header = headerValues[0]
                var newBaseUrl: HttpUrl? = null

                newBaseUrl = when (header) {
                    StringConstants.BLOCK_CHAIN_HEADER -> BLOCK_CHAIN_URL.toHttpUrlOrNull()
                    StringConstants.LIGHTNING_HEADER -> {
                        val host = request.header("host")
                        builder.removeHeader("host")
                        host?.toHttpUrlOrNull()
                    }
                    else -> BASE_URL.toHttpUrlOrNull()
                }
                val newUrl = oldUrl.newBuilder().scheme(newBaseUrl?.scheme!!)
                    .host(newBaseUrl.host)
                    .port(newBaseUrl.port)
                    .removePathSegment(0)
                    .build()
                return processResponse(chain.proceed(builder.url(newUrl).build()))
            } else {
                processResponse(chain.proceed(request))
            }
        }


        //访问网络之后，处理Response(这里没有做特别处理)
        private fun processResponse(response: Response): Response {
//            doHttpCode(response)
            return response
        }

        /**
         * 处理http code
         *
         * @param response
         */
        private fun doHttpCode(response: Response) {
            // 处理http code
            val statusCode = response.code
            if (statusCode != 200) {
                throw IOException()
            }
        }
    }

    object NullStringEmptyTypeAdapterFactory : TypeAdapterFactory {
        override fun <T> create(gson: Gson?, type: TypeToken<T>): TypeAdapter<T>? {
            val rawType = type.rawType as Class<T>
            return if (rawType != String::class.java) {
                null
            } else StringNullAdapter as TypeAdapter<T>
        }
    }

    object StringNullAdapter : TypeAdapter<String?>() {
        @Throws(IOException::class)
        override fun read(reader: JsonReader): String {
            if (reader.peek() === JsonToken.NULL) {
                reader.nextNull()
                return ""
            }
            return reader.nextString()
        }

        @Throws(IOException::class)
        override fun write(writer: JsonWriter, value: String?) {
            if (value == null) {
                writer.nullValue()
                return
            }
            writer.value(value)
        }
    }

    private object SingletonHolder {
        val INSTANCE = RetrofitHelper2()
    }

    class MyGsonRequestBodyConverter<T> internal constructor(
        private var gson: Gson,
        private var adapter: TypeAdapter<T>
    ) :
        Converter<T, RequestBody> {

        @Throws(IOException::class)
        override fun convert(value: T): RequestBody {
            val buffer = Buffer()
            val writer: Writer = OutputStreamWriter(buffer.outputStream(), UTF_8)
            val jsonWriter: JsonWriter = this.gson.newJsonWriter(writer)
            adapter.write(jsonWriter, value)
            jsonWriter.close()
            return buffer.readByteString().toRequestBody(MEDIA_TYPE)
        }

    }

    class MyGsonConverterFactory private constructor(gson: Gson) : Converter.Factory() {
        companion object {
            private lateinit var gson: Gson

            @JvmOverloads
            fun create(gson: Gson? = Gson()): MyGsonConverterFactory {
                if (gson == null) throw NullPointerException("gson == null")
                return MyGsonConverterFactory(gson)
            }
        }

        override fun responseBodyConverter(
            type: Type, annotations: Array<Annotation>,
            retrofit: Retrofit
        ): Converter<ResponseBody, *>? {
            return MyGsonResponseBodyConverter<Any>(gson, type)
        }

        override fun requestBodyConverter(
            type: Type,
            parameterAnnotations: Array<Annotation>,
            methodAnnotations: Array<Annotation>,
            retrofit: Retrofit
        ): Converter<*, RequestBody> {
            val adapter: TypeAdapter<*> = gson.getAdapter(TypeToken.get(type))
            return MyGsonRequestBodyConverter(gson, adapter)
        }

        init {
            Companion.gson = gson
        }
    }

    class MyGsonResponseBodyConverter<T>(gson: Gson, type: Type) :
        Converter<ResponseBody, T> {
        private var gson: Gson = gson
        private var type: Type = type

        @Throws(IOException::class)
        override fun convert(value: ResponseBody): T {
            val response = value.string()
            val jsonReader = JsonReader(StringReader(response))
            if (jsonReader.peek() == JsonToken.BEGIN_ARRAY) {
                val jsonObject = JSONObject()
                jsonObject.put("array", JSONArray(response))
                return gson.fromJson(jsonObject.toString(), type)
            }
            return value.use { value ->
                gson.fromJson(response, type)
            }
        }

    }
}