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
 *
 * 使用 fetch + ReadableStream 手动解析 SSE：后端 Spring SseEmitter 写出的数据行是 `data:xxx`，
 * 原生 EventSource 会按规范删除 `data:` 后的一个空格，导致模型按 token 返回的前导空格
 * （代码块中的空格、缩进等）丢失，因此这里保留 `data:` 后的原始字符。
 */
export function createSSE(options: SSEOptions): SSEController {
  const { url, onMessage, onError, onComplete, timeout = 180000 } = options
  const fullUrl = url.startsWith('http') ? url : `${API_BASE_URL}${url}`
  const abortController = new AbortController()

  let timer: number | null = null
  let hasReceivedData = false
  let finished = false

  const clearTimer = () => {
    if (timer !== null) {
      clearTimeout(timer)
      timer = null
    }
  }

  const close = () => {
    clearTimer()
    abortController.abort()
  }

  const finish = (callback: (() => void) | undefined) => {
    if (finished) return
    finished = true
    close()
    callback?.()
  }

  const startTimer = () => {
    clearTimer()
    timer = window.setTimeout(() => {
      finish(() => onError?.(new SSEError('timeout', '响应时间过长，请稍后再试')))
    }, timeout)
  }

  const consume = async () => {
    try {
      const response = await fetch(fullUrl, {
        signal: abortController.signal,
        headers: { Accept: 'text/event-stream' }
      })
      if (!response.ok) throw new Error(`HTTP ${response.status}`)
      if (!response.body) throw new Error('Empty response body')
      startTimer()

      const reader = response.body.getReader()
      const decoder = new TextDecoder('utf-8')
      let buffer = ''
      let dataLines: string[] = []

      const dispatch = () => {
        if (dataLines.length === 0) return
        const data = dataLines.join('\n')
        dataLines = []
        if (!data) return
        hasReceivedData = true
        onMessage?.(data)
      }

      while (true) {
        const { value, done } = await reader.read()
        if (done) break
        buffer += decoder.decode(value, { stream: true })
        let index: number
        while ((index = buffer.indexOf('\n')) !== -1) {
          const line = buffer.slice(0, index).replace(/\r$/, '')
          buffer = buffer.slice(index + 1)
          if (line === '') {
            dispatch()
          } else if (line.startsWith('data:')) {
            dataLines.push(line.slice(5))
          }
        }
      }

      buffer += decoder.decode()
      if (buffer) {
        const line = buffer.replace(/\r$/, '')
        if (line.startsWith('data:')) dataLines.push(line.slice(5))
      }
      dispatch()
      finish(onComplete)
    } catch (err) {
      if (finished || abortController.signal.aborted) return
      finish(() => onError?.(
        hasReceivedData
          ? new SSEError('unknown', err instanceof Error ? err.message : '连接异常')
          : new SSEError('network', '连接失败，请检查网络或稍后重试')
      ))
    }
  }

  startTimer()
  void consume()

  return { close }
}
