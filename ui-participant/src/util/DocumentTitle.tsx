import { useEffect } from 'react'

import { usePortalEnv } from 'providers/PortalProvider'

type DocumentTitleProps = {
  title?: string
}

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
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
