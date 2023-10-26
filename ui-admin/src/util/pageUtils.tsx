import React from 'react'

/** renders a page header with the appropriate styling and spacing */
export const renderPageHeader = (content: React.ReactNode) => {
  return <div className="d-flex mb-2">
    <h2 className="fw-bold">{content}</h2>
  </div>
}
