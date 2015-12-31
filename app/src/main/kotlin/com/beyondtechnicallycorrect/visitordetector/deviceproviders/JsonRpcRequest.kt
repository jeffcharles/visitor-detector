package com.beyondtechnicallycorrect.visitordetector.deviceproviders

class JsonRpcRequest(val jsonrpc: String, val id: String, val method: String, val params: Array<String>)
