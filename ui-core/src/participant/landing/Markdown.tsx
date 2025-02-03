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
      <ReactMarkdown components={{ a: LinkRenderer }}>{children}</ReactMarkdown>
    </div>
  )
}

/**
 * Makes all non-relative links open in a new tab.  Adapted from
 * https://stackoverflow.com/questions/69119798/react-markdown-links-dont-open-in-a-new-tab-despite-using-target-blank
 */
function LinkRenderer(props: { href?: string, children: React.ReactNode }) {
  return (
    <a href={props.href} target={props.href?.startsWith('/') ? '' : '_blank'} rel="noreferrer">
      {props.children}
    </a>
  )
}

type InlineMarkdownProps = {
  children: string
}

/** Render Markdown without a wrapping paragraph tag. */
export const InlineMarkdown = (props: InlineMarkdownProps) => {
  const { children } = props
  return <ReactMarkdown disallowedElements={['p']} unwrapDisallowed components={{ a: LinkRenderer }}>
    {children}
  </ReactMarkdown>
}
