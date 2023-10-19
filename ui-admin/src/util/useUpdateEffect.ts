import { useEffect, useRef } from 'react'

/**
 * A custom useEffect hook that only triggers on updates, not on initial mount
 * adapted from https://stackoverflow.com/questions/55075604/react-hooks-useeffect-only-on-update
 */
export default function useUpdateEffect(effect: React.EffectCallback, dependencies: React.DependencyList) {
  const isInitialMount = useRef(true)

  useEffect(() => {
    if (isInitialMount.current) {
      isInitialMount.current = false
    } else {
      return effect()
    }
  }, dependencies)
}
