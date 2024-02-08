import { MailingAddress } from '@juniper/ui-core'
import React, { useEffect, useState } from 'react'
import Api from '../api/api'
import { doApiLoad } from '../api/api-utils'
import { isEmpty } from 'lodash'

/**
 * Uses the autocomplete API to search for addresses while the user is typing.
 */
export default function AutocompleteAddressInput(
  {
    value,
    setValue,
    onSelectMailingAddress,
    inputClassName
  } : {
    value: string,
    setValue: (newVal: string) => void,
    onSelectMailingAddress: (mailingAddress: MailingAddress) => void,
    inputClassName?: string
  }
) {
  const [isFocused, setIsFocused] = useState<boolean>(false)
  const [results, setResults] = useState<MailingAddress[]>([])

  useEffect(() => {
    doApiLoad(
      async () => {
        const newResults = await Api.autocompleteAddress({
          search: value
        })
        setResults(newResults)
      })
  }, [value])
  return <div
    onFocus={() => setIsFocused(true)}
    onBlur={e => {
      if (!e.currentTarget.contains(e.relatedTarget)) {
        setIsFocused(false)
      }
    }}
    className={'w-100 position-relative'}
  >
    <input
      className={`${inputClassName} w-100`}
      value={value}
      onChange={e => setValue(e.target.value)}
    />
    { isFocused &&
        <div
          tabIndex={0}
          className={'w-100 position-absolute border border-1 bg-white rounded-1 my-1'}
        >
          {
            results.map((addr, idx) => <div
              key={idx}
              role={'button'}
              onClick={() => {
                onSelectMailingAddress(addr)
                setIsFocused(false) // remove dropdown upon click
              }}
            >
              <p className={'p-2 my-0'}>{formatAddress(addr)}</p>
              {idx !== results.length && <div className={'border-bottom border-1'}/>}
            </div>
            )
          }
        </div>
    }
  </div>
}

const formatAddress = (addr: MailingAddress)  => {
  const out: string[] = [
    addr.street1,
    addr.street2,
    addr.city,
    addr.state,
    addr.postalCode
  ]

  return out
    .filter(val => !isEmpty(val))
    .join(' ')
}
