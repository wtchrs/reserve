export function logOnDev(...data: any[]) {
    if (import.meta.env.MODE === 'development') {
        console.log(...data)
    }
}
