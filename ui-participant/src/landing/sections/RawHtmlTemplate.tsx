import React from 'react'

import { TemplateComponentProps } from 'util/templateUtils'

type RawHtmlTemplateProps = TemplateComponentProps

/**
 * renders raw html content
 * TODO -- determine whether we need to sanitize the content here or whether we trust our database and
 * @param content
 * @constructor
 */
export default function RawHtmlTemplate(props: RawHtmlTemplateProps) {
  const { anchorRef, rawContent } = props
  return <div id={anchorRef} dangerouslySetInnerHTML={{ __html: rawContent ? rawContent : '' }}></div>
}
