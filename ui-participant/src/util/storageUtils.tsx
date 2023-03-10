import { useState } from 'react'

const useStorage = (storage: Storage) => (key: string) => {
  const initialValue = storage.getItem(key)
  const [value, setValue] = useState<string | null>(initialValue)

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
