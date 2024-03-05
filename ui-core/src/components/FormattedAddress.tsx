import React from 'react'
import { MailingAddress } from '../types/address'
import { isEmpty, isNil } from 'lodash'


/**
 * TODO
 */
export default function FormattedAddress({ address } : {address: MailingAddress}) {
  const addressLines = [
    address.street1,
    address.street2,
    `${address.city || ''} ${address.state||''} ${address.postalCode ||''}`,
    address.country
  ]

  return <div>
    {
      addressLines
        .filter(line => !isNil(line) && !isEmpty(line.trim()))
        .map((line, idx) => (
          <p key={idx} className='my-0'>{line}</p>
        ))}
  </div>
}
