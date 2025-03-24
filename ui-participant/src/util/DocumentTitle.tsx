import { useEffect } from 'react'

import { usePortalEnv } from 'providers/PortalProvider'
import {
  getTranslatedPortalName,
  useI18n
} from '@juniper/ui-core'

type DocumentTitleProps = {
  title?: string
}

export const DocumentTitle = (props: DocumentTitleProps) => {
  const { title } = props
  const { portal } = usePortalEnv()

  const { i18n } = useI18n()

  const portalName = getTranslatedPortalName(i18n, portal.shortcode, portal.name)

  const fullTitle = title ? `${title} | ${portalName}` : portalName
  useEffect(() => {
    const previousTitle = document.title
    document.title = fullTitle
    return () => {
      document.title = previousTitle
    }
  }, [fullTitle])

  return null
}
