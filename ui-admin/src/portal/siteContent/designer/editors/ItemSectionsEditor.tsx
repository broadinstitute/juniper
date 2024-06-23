import React, { useId } from 'react'
import { ButtonConfig, HtmlSection, ItemSection, SectionConfig } from '@juniper/ui-core'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faChevronDown, faChevronUp, faPlus } from '@fortawesome/free-solid-svg-icons'
import { ListElementController } from '../components/ListElementController'
import { ButtonEditor } from './ButtonsEditor'
import classNames from 'classnames'

/**
 *
 */
export const ItemSectionsEditor = ({ section, updateSection }: {
    section: HtmlSection, updateSection: (section: HtmlSection) => void
}) => {
  const config = JSON.parse(section.sectionConfig || '{}') as SectionConfig
  const itemSections = config.itemSections as ItemSection[] || []
  const sectionsContentId = useId()
  const sectionsTargetSelector = `#${sectionsContentId}`

  return (
    <div>
      <div className="pb-1">
        <button
          aria-controls={sectionsTargetSelector}
          aria-expanded="false"
          className={classNames('btn w-100 py-2 px-0 d-flex text-decoration-none')}
          data-bs-target={sectionsTargetSelector}
          data-bs-toggle="collapse"
        >
          <span className={'form-label fw-semibold mb-0'}>Sections ({itemSections.length})</span>
          <span className="text-center px-2">
            <FontAwesomeIcon icon={faChevronDown} className="hidden-when-collapsed"/>
            <FontAwesomeIcon icon={faChevronUp} className="hidden-when-expanded"/>
          </span>
        </button>
      </div>
      <div className="collapse hide rounded-3 mb-2" id={sectionsContentId}
        style={{ backgroundColor: '#eee', padding: '0.75rem' }}>
        {itemSections.map((itemSection, index) => (
          <div key={index}>
            <div className="d-flex justify-content-between align-items-center">
              <span className="h5">Edit item section</span>
              <ListElementController<ItemSection>
                index={index}
                items={itemSections}
                updateItems={newSections => {
                  updateSection({
                    ...section,
                    sectionConfig: JSON.stringify({ ...config, itemSections: newSections })
                  })
                }}
              />
            </div>
            <ItemSectionEditor
              itemSection={itemSection}
              updateItemSection={updatedItemSection => {
                const newSections = [...itemSections]
                newSections[index] = updatedItemSection
                updateSection({
                  ...section,
                  sectionConfig: JSON.stringify({ ...config, itemSections: newSections })
                })
              }}
            />
          </div>
        ))}
        <Button onClick={() => {
          const newSections = [...itemSections, { title: '', items: [] }]
          updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, itemSections: newSections }) })
        }}>
          <FontAwesomeIcon icon={faPlus}/> Add Section
        </Button>
      </div>
    </div>
  )
}

const ItemSectionEditor = ({ itemSection, updateItemSection }: {
    itemSection: ItemSection, updateItemSection: (itemSection: ItemSection) => void
}) => {
  const itemssContentId = useId()
  const itemsTargetSelector = `#${itemssContentId}`
  return (
    <div style={{ backgroundColor: '#ddd', padding: '0.75rem' }} className="rounded-3 mb-2">
      <label className='form-label fw-semibold'>Title</label>
      <input type='text' className='form-control mb-2' value={itemSection.title || ''}
        onChange={e => updateItemSection({ ...itemSection, title: e.target.value })}/>
      <div className="pb-1">
        <button
          aria-controls={itemsTargetSelector}
          aria-expanded="false"
          className={classNames('btn w-100 py-2 px-0 d-flex text-decoration-none')}
          data-bs-target={itemsTargetSelector}
          data-bs-toggle="collapse"
        >
          <span className={'form-label fw-semibold mb-0'}>Buttons ({itemSection.items.length})</span>
          <span className="text-center px-2">
            <FontAwesomeIcon icon={faChevronDown} className="hidden-when-collapsed"/>
            <FontAwesomeIcon icon={faChevronUp} className="hidden-when-expanded"/>
          </span>
        </button>
      </div>
      <div className="collapse hide rounded-3 mb-2" id={itemssContentId}>
        {itemSection.items.map((item, i) => {
          return <div key={i} className="rounded-3 mb-2"
            style={{ backgroundColor: '#eee', padding: '0.75rem' }}>
            <div className="d-flex justify-content-between align-items-center">
              <span className="h5">Edit item</span>
              <ListElementController<ButtonConfig>
                index={i}
                items={itemSection.items}
                updateItems={newItems => {
                  updateItemSection({ ...itemSection, items: newItems })
                }}
              />
            </div>
            <ButtonEditor button={item} updateButton={newButton => {
              const newItems = [...itemSection.items]
              newItems[i] = newButton
              updateItemSection({ ...itemSection, items: newItems })
            }}/>
          </div>
        })}
        <Button onClick={() => {
          const newItems = [...itemSection.items]
          newItems.push({ type: 'internalLink', text: '', href: '' })
          updateItemSection({ ...itemSection, items: newItems })
        }}><FontAwesomeIcon icon={faPlus}/> Add Button</Button>
      </div>
    </div>
  )
}
