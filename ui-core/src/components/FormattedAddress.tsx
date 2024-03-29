import React from 'react'
import { MailingAddress } from '../types/address'
import { toAddressLines } from '../addressUtils'
import { diffWords } from 'diff'
import classNames from 'classnames'

/**
 * Formats the address for display. If showDiff is provided, it will highlight the
 * differences between the two addresses.
 * <br/>
 * Future work may include internationalizing address formatting.
 */
export default function FormattedAddress({
  address,
  showDiff
}: {
  address: MailingAddress,
  showDiff?: MailingAddress
}) {
  const addressLines = toAddressLines(address)
  const diffLines = showDiff ? toAddressLines(showDiff) : addressLines
  const changes = diffWords(diffLines.join('\n'), addressLines.join('\n'))


  return <div>
    {
      changes.filter(change => !change.removed).map((change, changeIdx) => {
        return <span
          key={`${changeIdx}`}
          className={classNames({
            'mark': change.added || change.removed
          })}
          style={{ whiteSpace: 'pre-line' }}
        >
          {change.value}
        </span>
      })
    }
  </div>
}


