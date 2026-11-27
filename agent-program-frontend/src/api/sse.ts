import { API_BASE_URL } from './config'

export type SSEErrorType = 'timeout' | 'network' | 'unknown'

export class SSEError extends Error {
  constructor(
    public type: SSEErrorType,
    message: string
  ) {
    super(message)
    this.name = 'SSEError'
  }
}

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
  let hasReceivedData = false

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
      onError?.(new SSEError('timeout', '响应时间过长，请稍后再试'))
    }, timeout)
  }

  es.onmessage = (event) => {
    if (event.data) {
      hasReceivedData = true
    }
    onMessage?.(event.data)
  }

  es.onerror = () => {
    clearTimer()
    // 服务端正常流式结束后往往会触发 error 并重连（readyState === CONNECTING）。
    // 若已经收到过数据，则视为正常结束；从未收到数据才当作真正的连接异常。
    if (es.readyState === EventSource.CLOSED || hasReceivedData) {
      onComplete?.()
    } else {
      onError?.(new SSEError('network', '连接失败，请检查网络或稍后重试'))
    }
    es.close()
  }

  return { close }
}
