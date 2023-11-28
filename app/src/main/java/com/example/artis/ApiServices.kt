package com.example.artis

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ApiServices(private val context: Context) {

    private val url = "https://api.openai.com/v1/completions"

    fun getResponseFromGPT(
        query: String,
        onResponseReceived: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val queue: RequestQueue = Volley.newRequestQueue(context)
        val jsonObject: JSONObject = JSONObject()
        jsonObject.put("model", "text-davinci-003")
        jsonObject.put("prompt", query)
        jsonObject.put("temperature", 0)
        jsonObject.put("max_tokens", 100)
        jsonObject.put("top_p", 1)
        jsonObject.put("frequency_penalty", 0.0)
        jsonObject.put("presence_penalty", 0.0)

        val postRequest = object : JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            Response.Listener { response ->
                val responseMsg: String =
                    response.getJSONArray("choices").getJSONObject(0).getString("text")
                onResponseReceived.invoke(responseMsg)
            },
            Response.ErrorListener { error ->
                onError.invoke("Error is: ${error.message}\n$error")
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Content-Type"] = "application/json"
                params["Authorization"] = "Bearer "
                return params
            }
        }

        queue.add(postRequest)
    }
}
