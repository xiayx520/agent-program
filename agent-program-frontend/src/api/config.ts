/** 接口基础地址，默认使用相对路径，可通过 .env 文件配置 VITE_API_BASE_URL */
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

/** 接口超时时间（后端约定 180000L） */
export const API_TIMEOUT = 180000
