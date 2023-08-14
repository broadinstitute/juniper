import { useEffect } from 'react'

/** TODO JSdoc */
export function useAutosaveEffect(saveFn: () => void, autoSaveInterval: number) {
  useEffect(() => {
    let timeoutHandle: number
    // run saveFn at the specified interval
    (function loop() {
      timeoutHandle = window.setTimeout(() => {
        saveFn()
        loop()
      }, autoSaveInterval)
    })()
    return () => {
      window.clearTimeout(timeoutHandle)
    }
  }, [])
}
