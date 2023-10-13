import { useEffect } from 'react'

/** Executes the save function at the specified interval, but only starting the interval after the
 * previous call returns, guaranteed no overlapping saves */
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
  }, [saveFn])
}
