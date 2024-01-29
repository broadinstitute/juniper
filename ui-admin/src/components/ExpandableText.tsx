import React, { useState } from 'react'

/**
 *
 */
export default function ExpandableText(
  { text, maxLen } : { text: string, maxLen: number }
) {
  const [showAll, setShowAll] = useState<boolean>(false)

  if (text.length < maxLen) {
    return <span>{text}</span>
  }

  if (showAll) {
    return <span>
      {text}
      <button className="btn btn-link m-0 p-0 ms-1" onClick={() => setShowAll(false)}>View Less</button>
    </span>
  }

  return <span>
    {text.slice(0, maxLen - 3)}...
    <button className="btn btn-link m-0 p-0 ms-1" onClick={() => setShowAll(true)}>View More</button>
  </span>
}
