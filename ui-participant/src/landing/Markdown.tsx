import React from 'react'
import ReactMarkdown from 'react-markdown'

type InlineMarkdownProps = {
  children: string
}

/** Render Markdown without a wrapping paragraph tag. */
export const InlineMarkdown = (props: InlineMarkdownProps) => {
  const { children } = props
  return <ReactMarkdown disallowedElements={['p']} unwrapDisallowed>{children}</ReactMarkdown>
}
