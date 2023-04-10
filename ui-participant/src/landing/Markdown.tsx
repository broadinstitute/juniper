import classNames from 'classnames'
import React, { CSSProperties } from 'react'
import ReactMarkdown from 'react-markdown'

type MarkdownProps = {
  children: string
  className?: string
  style?: CSSProperties
}

/** Render Markdown. */
export const Markdown = (props: MarkdownProps) => {
  const { children, className, style } = props
  return (
    <div className={classNames('markdown', className)} style={style}>
      <ReactMarkdown>{children}</ReactMarkdown>
    </div>
  )
}

type InlineMarkdownProps = {
  children: string
}

/** Render Markdown without a wrapping paragraph tag. */
export const InlineMarkdown = (props: InlineMarkdownProps) => {
  const { children } = props
  return <ReactMarkdown disallowedElements={['p']} unwrapDisallowed>{children}</ReactMarkdown>
}
