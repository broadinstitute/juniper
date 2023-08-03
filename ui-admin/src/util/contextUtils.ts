/** placeholder function for using in contexts that can't be full initialized until the provider is rendered */
export function emptyContextAlertFunction() {
    alert('Error, context used outside of scope')
    return Promise.resolve(null)
}