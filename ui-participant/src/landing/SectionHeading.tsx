import classNames from 'classnames'
import React, { useEffect, useRef, useState } from 'react'

const sectionHeadingClassname = 'section-heading'

type SectionHeadingProps = JSX.IntrinsicElements['h1'] & {
  onDetermineLevel?: (level: 1 | 2) => void
}

/** A heading for a landing page section. */
export const SectionHeading = (props: SectionHeadingProps) => {
  const { onDetermineLevel, className, ...otherProps } = props

  const [level, setLevel] = useState<1 | 2>()

  const placeholderElement = useRef<HTMLDivElement | null>(null)
  useEffect(() => {
    const allSectionHeadings = document.querySelectorAll(`.${sectionHeadingClassname}`)
    const level = placeholderElement.current === allSectionHeadings[0] ? 1 : 2
    setLevel(level)
    onDetermineLevel?.(level)
  }, [])

  if (level === undefined) {
    return <div ref={placeholderElement} className={sectionHeadingClassname} />
  }

  const Tag: 'h1' | 'h2' = `h${level}`
  return <Tag {...otherProps} className={classNames(sectionHeadingClassname, className)} />
}
