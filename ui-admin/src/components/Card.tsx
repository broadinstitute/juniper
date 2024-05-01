import React from 'react'
import { isEmpty } from 'lodash'


/**
 *
 */
export function Card({ children }: { children: React.ReactNode }) {
  return <div className="card w-75 border shadow-sm mb-3">
    {children}
  </div>
}

/**
 *
 */
export function CardHeader({ children }: { children: React.ReactNode }) {
  return <div className="card-header border-bottom bg-white d-flex flex-row align-items-center">
    {children}
  </div>
}

/**
 *
 */
export function CardTitle({ title }: { title: string }) {
  return <div className="fw-bold lead my-1">{title}</div>
}

/**
 *
 */
export function CardBody({ children }: { children: React.ReactNode }) {
  return <div className="card-body d-flex flex-row flex-wrap">
    {children}
  </div>
}

/**
 * One row of the profile's data.
 */
export function CardRow(
  { title, children }: {
        title: string,
        children: React.ReactNode
    }
) {
  return <>
    <div className="w-25 fw-bold mb-4 mt-2" aria-label={title}>
      {title}
    </div>
    <div className="w-75 mb-4">
      {children}
    </div>
  </>
}


/**
 * Row of readonly data, where the title takes the leftmost portion and the values are on the rightmost.
 */
export function CardValueRow(
  { title, values }: {
        title: string,
        values: string[]
    }
) {
  return <CardRow title={title}>
    {(isEmpty(values) || values.every(isEmpty)) && <p className="fst-italic mb-0 mt-2 text-muted">None provided</p>}
    {
      values.filter(val => !isEmpty(val)).map((val, idx) => (
        <p key={idx} className={`mb-0 ${idx == 0 ? 'mt-2' : ''}`}>{val}</p>
      ))
    }
  </CardRow>
}
