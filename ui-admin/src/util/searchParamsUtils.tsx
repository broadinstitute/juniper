import { useSearchParams } from 'react-router-dom'

/**
 * A hook to get and set a single search param.
 */
export function useSingleSearchParam(paramKey: string): [string | null, (newValue: string | null) => void] {
  const [searchParams, setSearchParams] = useSearchParams()

  const value = searchParams.get(paramKey)

  const setValue = (newValue: string | null) => {
    setSearchParams(params => {
      if (newValue === null) {
        params.delete(paramKey)
      } else {
        params.set(paramKey, newValue.toString())
      }
      return params
    })
  }

  return [value, setValue]
}
