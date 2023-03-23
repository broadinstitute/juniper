import React, { PropsWithChildren, useEffect, useState } from 'react'

type DelayedRenderProps = PropsWithChildren<{
  delay?: number
}>

export const DelayedRender = (props: DelayedRenderProps) => {
  const { children, delay = 150 } = props

  const [shouldRender, setShouldRender] = useState(false)
  useEffect(() => {
    setTimeout(() => {
      setShouldRender(true)
    }, delay)
  }, [])

  return shouldRender ? <>{children}</> : null
}
