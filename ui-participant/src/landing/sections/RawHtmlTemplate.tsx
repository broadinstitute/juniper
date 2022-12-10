import React from 'react'

/**
 * renders raw html content
 * TODO -- determine whether we need to sanitize the content here or whether we trust our database and
 * @param content
 * @constructor
 */
export default function RawHtmlTemplate({ rawContent }: {rawContent: string | null}) {
  return <div dangerouslySetInnerHTML={{ __html: rawContent ? rawContent : '' }}></div>
}
