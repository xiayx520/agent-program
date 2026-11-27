/** 生成会话唯一标识 */
export function generateChatId(): string {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`
}
