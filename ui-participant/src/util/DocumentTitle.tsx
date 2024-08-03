import { useEffect } from 'react'

import { usePortalEnv } from 'providers/PortalProvider'

type DocumentTitleProps = {
  title?: string
}

export const DocumentTitle = (props: DocumentTitleProps) => {
  const { title } = props
  const { portal } = usePortalEnv()

  const fullTitle = title ? `${title} | ${portal.name}` : portal.name
  useEffect(() => {
    const previousTitle = document.title
    document.title = fullTitle
    return () => {
      document.title = previousTitle
    }
  }, [fullTitle])

  return null
}
