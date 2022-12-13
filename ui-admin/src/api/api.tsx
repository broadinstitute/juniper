import * as queryString from 'query-string'


export type AdminUser = {
  username: string,
  token: string
};

let bearerToken: string | null = null
export const API_ROOT = process.env.REACT_APP_API_ROOT

export default {
  getInitHeaders() {
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    }
    if (bearerToken !== null) {
      headers['Authorization'] = `Bearer ${bearerToken}`
    }
    return headers
  },

  getGetInit() {
    return {
      headers: this.getInitHeaders(),
      method: 'GET'
    }
  },

  async processJsonResponse(response: any) {
    const obj = await response.json()
    if (response.ok) {
      return obj
    }
    return Promise.reject(response)
  },

  async unauthedLogin(username: string): Promise<AdminUser> {
    const url =`${API_ROOT}/current-user/v1/unauthed-login?${  new URLSearchParams({
      username
    })}`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders()
    })
    return await this.processJsonResponse(response)
  },

  async tokenLogin(token: string): Promise<AdminUser> {
    const url =`${API_ROOT}/current_user/token_login`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify({ token })
    })
    return await this.processJsonResponse(response)
  },

  setBearerToken(token: string | null) {
    bearerToken = token
  }
}
