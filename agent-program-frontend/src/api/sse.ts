import { API_BASE_URL } from './config'

export interface SSEOptions {
  /** SSE 请求地址（相对或绝对） */
  url: string
  /** 收到流式数据回调 */
  onMessage?: (data: string) => void
  /** 连接异常回调 */
  onError?: (error: Error) => void
  /** 连接正常结束回调 */
  onComplete?: () => void
  /** 整体超时时间，默认 180000ms */
  timeout?: number
}

export interface SSEController {
  close: () => void
}

/**
 * 建立 SSE 连接并实时消费流式数据
 * 使用原生 EventSource，连接关闭时通过 readyState 区分正常结束与异常
 */
export function createSSE(options: SSEOptions): SSEController {
  const { url, onMessage, onError, onComplete, timeout = 180000 } = options
  const fullUrl = url.startsWith('http') ? url : `${API_BASE_URL}${url}`
  const es = new EventSource(fullUrl)

  let timer: number | null = null

  const clearTimer = () => {
    if (timer !== null) {
      clearTimeout(timer)
      timer = null
    }
  }

  const close = () => {
    clearTimer()
    es.close()
  }

  es.onopen = () => {
    clearTimer()
    timer = window.setTimeout(() => {
      close()
      onError?.(new Error('连接超时，请稍后重试'))
    }, timeout)
  }

  es.onmessage = (event) => {
    onMessage?.(event.data)
  }

  es.onerror = () => {
    clearTimer()
    // readyState === CLOSED 表示服务端已正常关闭连接
    if (es.readyState === EventSource.CLOSED) {
      onComplete?.()
    } else {
      onError?.(new Error('SSE 连接发生错误'))
    }
    es.close()
  }

  return { close }
}
