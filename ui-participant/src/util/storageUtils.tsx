import { useState } from 'react'

// TODO: figure out how to make TypeScript happy so we can use this HoF below instead of copy/paste
// eslint-disable-next-line @typescript-eslint/no-unused-vars
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

export const useLocalStorage = (key: string) => {
  const initialValue = localStorage.getItem(key)
  const [value, setValue] = useState<string | null>(initialValue)

  const setValueAndLocalStorage = (newValue: string | null) => {
    if (newValue === null || newValue === undefined) {
      localStorage.removeItem(key)
    } else {
      localStorage.setItem(key, newValue)
    }
    setValue(newValue)
  }

  return [value, setValueAndLocalStorage] as const
}

export const useSessionStorage = (key: string) => {
  const initialValue = sessionStorage.getItem(key)
  const [value, setValue] = useState<string | null>(initialValue)

  const setValueAndSessionStorage = (newValue: string | null) => {
    if (newValue === null || newValue === undefined) {
      sessionStorage.removeItem(key)
    } else {
      sessionStorage.setItem(key, newValue)
    }
    setValue(newValue)
  }

  return [value, setValueAndSessionStorage] as const
}
