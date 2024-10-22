export function getAccessToken() {
    return localStorage.getItem('auth')
}

export function setAccessToken(token: string) {
    localStorage.setItem('auth', token)
    window.dispatchEvent(new CustomEvent('authChanged', {detail: token}))
}

export function removeAccessToken() {
    localStorage.removeItem('auth')
    window.dispatchEvent(new CustomEvent('authChanged', {detail: null}))
}
