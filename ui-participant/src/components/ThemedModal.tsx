import React from 'react'
import { ModalProps } from 'react-bootstrap'
import Modal from 'react-bootstrap/Modal'
import { usePortalEnv } from '../providers/PortalProvider'
import { BrandConfiguration, brandStyles } from '../util/brandUtils'

/**
 * react-bootstrap creates modals outside the <body> tag, which
 * means that they are outside the div which provides brand
 * styling. This modal manually adds theming back to any of the
 * contents of the modal.
 */
export default function ThemedModal(
  props: ModalProps & { children : React.ReactNode }
) {
  const { localContent } = usePortalEnv()

  const brandConfig: BrandConfiguration = {}
  if (localContent.primaryBrandColor) {
    brandConfig.brandColor = localContent.primaryBrandColor
  }

  return <Modal {...props}>
    <div
      style={brandStyles(brandConfig)}
    >
      {props.children}
    </div>
  </Modal>
}
