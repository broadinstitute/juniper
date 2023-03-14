import { useState } from 'react'

const useStorage = (storage: Storage) => (key: string) => {
  const [value, setValue] = useState<string | null>(() => storage.getItem(key))

  const setValueAndStorage = (newValue: string | null) => {
    if (newValue === null || newValue === undefined) {
      storage.removeItem(key)
    } else {
      storage.setItem(key, newValue)
    }
    setValue(newValue)
  }

  return [value, setValueAndStorage] as const
}

export const useLocalStorage = useStorage(localStorage)
export const useSessionStorage = useStorage(sessionStorage)
