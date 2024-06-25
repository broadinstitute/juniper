import React, { useId } from 'react'
import { ButtonConfig, HtmlSection, ItemSection, SectionConfig } from '@juniper/ui-core'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons'
import { ListElementController } from '../components/ListElementController'
import { ButtonEditor } from './ButtonsEditor'
import { CollapsibleSectionButton } from '../components/CollapsibleSectionButton'

/**
 * Editor for footer item sections
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
      <CollapsibleSectionButton targetSelector={sectionsTargetSelector}
        sectionLabel={`Sections (${itemSections.length})`}/>
      <div className="collapse hide rounded-3 mb-2" id={sectionsContentId}
        style={{ backgroundColor: '#eee', padding: '0.75rem' }}>
        {itemSections.map((itemSection, index) => (
          <div key={index}>
            <div className="d-flex justify-content-between align-items-center">
              <span className="h6">Edit item section</span>
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
  const itemsContentId = useId()
  const itemsTargetSelector = `#${itemsContentId}`
  return (
    <div style={{ backgroundColor: '#ddd', padding: '0.75rem' }} className="rounded-3 mb-2">
      <label className='form-label fw-semibold'>Title</label>
      <input type='text' className='form-control mb-2' value={itemSection.title || ''}
        onChange={e => updateItemSection({ ...itemSection, title: e.target.value })}/>
      <CollapsibleSectionButton targetSelector={itemsTargetSelector}
        sectionLabel={`Buttons (${itemSection.items.length})`}/>
      <div className="collapse hide rounded-3 mb-2" id={itemsContentId}>
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
