import { SetStateAction, useCallback, useEffect, useRef, useState } from 'react'

type Callback<T> = (value?: T) => void;
type DispatchWithCallback<T> = (value: T, callback?: Callback<T>) => void;

/**
 * Normally, we use useEffect to do something after state changes.  But sometimes we want particular
 * code to run after particular paths for a state change, and passing a callback function to setState
 * for the thing to do afterwards is cleaner than having custom detection logic in a useEffect.
 *
 * adapted from https://stackoverflow.com/questions/56247433/how-to-use-setstate-callback-on-react-hooks
 * @param initialState
 */
function useStateCallback<T>(initialState: T | (() => T)): [T, DispatchWithCallback<SetStateAction<T>>] {
  const [state, _setState] = useState(initialState)

  const callbackRef = useRef<Callback<T>>()
  const isFirstCallbackCall = useRef<boolean>(true)

  const setState = useCallback((setStateAction: SetStateAction<T>, callback?: Callback<T>): void => {
    callbackRef.current = callback
    _setState(setStateAction)
  }, [])

  useEffect(() => {
    if (isFirstCallbackCall.current) {
      isFirstCallbackCall.current = false
      return
    }
    callbackRef.current?.(state)
  }, [state])

  return [state, setState]
}

export default useStateCallback
