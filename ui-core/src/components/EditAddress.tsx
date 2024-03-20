import { AddressValidationResult, MailingAddress } from 'src/types/address'
import classNames from 'classnames'
import { isNil, sortBy } from 'lodash'
import React, { useEffect, useState } from 'react'
import { getAllCountries } from '../i18nUtils'
import { explainAddressValidationResults, isAddressFieldValid } from '../addressUtils'
import CreatableSelect from 'react-select/creatable'

/**
 * Renders a set of fields for editing mailing addresses. If provided an address validation result,
 * will also display errors.
 */
export function EditAddress(
  {
    mailingAddress,
    setMailingAddress,
    validationResult,
    showLabels = true,
    showErrors = true,
    language = 'en' // todo: accept internationalized labels, see JN-910 for implementation
  }: {
    mailingAddress: MailingAddress,
    setMailingAddress: (updated: React.SetStateAction<MailingAddress>) => void,
    validationResult?: AddressValidationResult,
    showLabels?: boolean,
    showErrors?: boolean,
    language: string
  }
) {
  const [hasChangedSinceValidation, setHasChangedSinceValidation] = useState<string[]>([])

  const onFieldChange = (field: keyof MailingAddress, value: string) => {
    if (!hasChangedSinceValidation.includes(field)) {
      setHasChangedSinceValidation(val => val.concat([field]))
    }

    setMailingAddress(old => {
      return {
        ...old,
        [field]: value
      }
    })
  }

  useEffect(() => {
    setHasChangedSinceValidation([])
  }, [validationResult])

  const countryOptions = getAllCountries(language).map(
    country => {
      return {
        value: country.code,
        label: country.name
      }
    }
  )

  const findCountryLabel = (val: string) => {
    const option = countryOptions.find(option => option.value === val)

    if (option) {
      return option.label
    }

    return val
  }

  // checks if a field should be in an error state; if true,
  // should be green, if false, red, and if undefined, unhighlighted
  const isValid = (field: keyof MailingAddress): boolean | undefined => {
    if (!validationResult || !isNil(validationResult.suggestedAddress)) {
      return undefined
    }

    if (!hasChangedSinceValidation.includes(field)
      && validationResult.valid
      && isNil(validationResult.suggestedAddress)
      && (isNil(validationResult.hasInferredComponents)
        || !validationResult.hasInferredComponents)) {
      return true
    }

    if (!hasChangedSinceValidation.includes(field)
      && !isAddressFieldValid(validationResult, field)) {
      return false
    }

    return undefined
  }

  return <>
    <div className='row mb-2'>
      <div className="col">
        <label
          htmlFor={'street1'}
          className={'fs-6 fw-bold'}
          hidden={!showLabels}
        >
          Street 1
        </label>
        <input
          className={classNames(
            'form-control', {
              'is-valid': isValid('street1') === true,
              'is-invalid': isValid('street1') === false
            })}
          type="text"
          id="street1"
          value={mailingAddress.street1 || ''}
          placeholder={'Street 1'}
          onChange={e => onFieldChange('street1', e.target.value)}/>
      </div>
    </div>
    <div className='row mb-2'>
      <label
        htmlFor={'street2'}
        className={'fs-6 fw-bold'}
        hidden={!showLabels}
      >
        Street 2
      </label>
      <div className="col">
        <input
          className={classNames(
            'form-control', {
              'is-valid': isValid('street2') === true,
              'is-invalid': isValid('street2') === false
            })}
          type="text" value={mailingAddress.street2 || ''}
          id="street2"
          placeholder={'Street 2'}
          onChange={e => onFieldChange('street2', e.target.value)}/>
      </div>
    </div>
    <div className='row mb-2'>
      <div className="col">
        <label
          htmlFor={'city'}
          className={'fs-6 fw-bold'}
          hidden={!showLabels}
        >
          City
        </label>
        <input
          className={classNames(
            'form-control', {
              'is-valid': isValid('city') === true,
              'is-invalid': isValid('city') === false
            })}
          type="text" value={mailingAddress.city || ''}
          id="city"
          placeholder={'City'}
          onChange={e => onFieldChange('city', e.target.value)}/>
      </div>
      <div className='col'>
        <label
          htmlFor={'state'}
          className={'fs-6 fw-bold'}
          hidden={!showLabels}
        >
          State
        </label>
        <input
          className={classNames(
            'form-control', {
              'is-valid': isValid('state') === true,
              'is-invalid': isValid('state') === false
            })}
          type="text" value={mailingAddress.state || ''}
          id="state"
          placeholder={'State/Province'}
          onChange={e => onFieldChange('state', e.target.value)}/>
      </div>
    </div>
    <div className='row'>
      <div className="col">
        <label
          htmlFor={'postalCode'}
          className={'fs-6 fw-bold'}
          hidden={!showLabels}
        >
          Postal Code
        </label>
        <input
          className={classNames(
            'form-control', {
              'is-valid': isValid('postalCode') === true,
              'is-invalid': isValid('postalCode') === false
            })}
          type="text" value={mailingAddress.postalCode || ''}
          id="postalCode"
          placeholder={'Postal Code'}
          onChange={e => onFieldChange('postalCode', e.target.value)}/>
      </div>
      <div className='col'>
        <label
          htmlFor={'country'}
          className={'fs-6 fw-bold'}
          hidden={!showLabels}
        >
          Country
        </label>
        <CreatableSelect
          styles={{
            control: baseStyles => ({
              ...baseStyles,
              borderColor: 'var(--bs-border-color)' // use same border color as all other components
            })
          }}
          placeholder={'Country'}
          id="country"
          options={sortBy(countryOptions, opt => opt.label)}
          value={{
            value: mailingAddress.country,
            label: findCountryLabel(mailingAddress.country)
          }}
          formatCreateLabel={val => val}
          onChange={val => onFieldChange('country', val ? val.value : '')}
        />
      </div>
      {showErrors && <div className={'mt-2 mb-0'}>
        {explainAddressValidationResults(validationResult).map(
          (explanation, idx) => {
            return <p key={idx} className='text-danger'>{explanation}</p>
          }
        )}
      </div>}

    </div>
  </>
}
