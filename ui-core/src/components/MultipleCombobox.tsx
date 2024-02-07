import { faChevronDown, faTimes } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { useVirtualizer } from '@tanstack/react-virtual'
import classNames from 'classnames'
import { useCombobox, useMultipleSelection } from 'downshift'
import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { trimStart } from 'lodash'

type MultipleComboboxProps<ComboboxItem> = {
  id?: string
  initialValue?: ComboboxItem[]
  itemToString: (item: ComboboxItem) => string
  options: ComboboxItem[]
  choicesByUrl?: string
  placeholder?: string
  onChange?: (value: ComboboxItem[]) => void
}

/**
 * Renders a multiple selection combobox. This virtualizes the options list to support
 * many more options than a normal React-select would, while remaining performant.
 */
export const MultipleComboBox = <ComboboxItem, >(props: MultipleComboboxProps<ComboboxItem>) => {
  const { id, initialValue, itemToString, options, choicesByUrl, placeholder, onChange } = props
  const [optionsList, setOptionsList] = useState<ComboboxItem[]>([])

  const loadChoicesByUrl = async (choicesByUrl: string) => {
    const response = await fetch(choicesByUrl)
    if (response.ok) {
      const jsonResponse = await response.json()
      const choices = jsonResponse.map((item: string) => ({ value: item, text: item }))
      setOptionsList(choices)
    } else {
      setOptionsList([])
    }
  }

  useEffect(() => {
    // Load options from choicesByUrl if provided, otherwise use any options that were provided in the question JSON.
    if (choicesByUrl) {
      loadChoicesByUrl(choicesByUrl)
    } else {
      setOptionsList(options)
    }
  }, [])

  const [inputValue, setInputValue] = useState('')
  const [selectedItems, _setSelectedItems] = useState<ComboboxItem[]>(
    initialValue || []
  )

  const setSelectedItems = useCallback((value: ComboboxItem[]) => {
    _setSelectedItems(value)
    onChange?.(value)
  }, [onChange])

  // Filter options to those that match input value.
  const items = useMemo(
    () => optionsList.filter(item => itemToString(item).toLowerCase().includes(trimStart(inputValue.toLowerCase()))),
    [optionsList, inputValue, itemToString]
  )

  // Store width of items in the combobox menu. This will be used to calculate
  // the height of each item. The initial value of 548 is the input width on
  // a window wide enough for the survey to reach its max width.
  const [itemWidth, setItemWidth] = useState(548)

  // Calculate height of each item.
  const itemHeights = useMemo(() => {
    const tempEl = document.createElement('div')
    document.body.appendChild(tempEl)

    // In order to get an accurate estimate, the temporary div must be
    // styled the same as one of the combobox items.
    tempEl.className = 'py-2 px-3 invisible'
    tempEl.style.boxSizing = 'border-box'
    tempEl.style.width = `${itemWidth}px`
    tempEl.style.fontFamily = '"Open Sans", "Helvetica Neue", Helvetica, Arial, sans-serif'
    tempEl.style.fontSize = '1rem'
    tempEl.style.fontWeight = '400'
    tempEl.style.lineHeight = '24px'

    const heights = items.map(item => {
      tempEl.innerText = itemToString(item)
      return tempEl.getBoundingClientRect().height
    })

    document.body.removeChild(tempEl)
    return heights
  }, [items, itemToString, itemWidth])

  const listRef = useRef<HTMLElement>(null)

  const virtualizer = useVirtualizer({
    count: items.length,
    getScrollElement: () => listRef.current,
    estimateSize: index => itemHeights[index]
  })

  const { getSelectedItemProps, getDropdownProps, removeSelectedItem } =
    useMultipleSelection<ComboboxItem>({
      selectedItems,
      onStateChange({ selectedItems: newSelectedItems, type }) {
        switch (type) {
          case useMultipleSelection.stateChangeTypes.SelectedItemKeyDownBackspace:
          case useMultipleSelection.stateChangeTypes.SelectedItemKeyDownDelete:
          case useMultipleSelection.stateChangeTypes.DropdownKeyDownBackspace:
          case useMultipleSelection.stateChangeTypes.FunctionRemoveSelectedItem:
            setSelectedItems(newSelectedItems || [])
            break
          default:
            break
        }
      }
    })

  const {
    isOpen,
    getToggleButtonProps,
    getMenuProps,
    getInputProps,
    highlightedIndex,
    getItemProps
  } = useCombobox<ComboboxItem>({
    inputId: id,
    inputValue,
    items,
    itemToString: item => (item ? itemToString(item) : ''),
    defaultHighlightedIndex: 0, // After selection, highlight the first item.
    selectedItem: null,
    stateReducer(state, actionAndChanges) {
      const { changes, type } = actionAndChanges

      switch (type) {
        case useCombobox.stateChangeTypes.InputKeyDownEnter:
        case useCombobox.stateChangeTypes.ItemClick:
          return {
            ...changes,
            isOpen: true, // Keep the menu open after selection.
            highlightedIndex: 0 // With the first option highlighted.
          }
        default:
          return changes
      }
    },
    // Scroll the highlighted item into view when navigating the
    // list using arrow keys.
    onHighlightedIndexChange: ({ highlightedIndex, type }) => {
      if (
        type !== useCombobox.stateChangeTypes.MenuMouseLeave &&
        highlightedIndex !== undefined
      ) {
        virtualizer.scrollToIndex(highlightedIndex)
      }
    },
    onStateChange({
      inputValue: newInputValue,
      type,
      selectedItem: newSelectedItem
    }) {
      switch (type) {
        case useCombobox.stateChangeTypes.InputKeyDownEnter:
        case useCombobox.stateChangeTypes.ItemClick:
          if (newSelectedItem) {
            setSelectedItems([...selectedItems, newSelectedItem])
            // Clear input to search for next value.
            setInputValue('')
          }
          break
        case useCombobox.stateChangeTypes.InputChange:
          setInputValue(newInputValue || '')
          break
        case useCombobox.stateChangeTypes.InputBlur:
          // Clear input so full list of options is shown when refocused.
          setInputValue('')
          break
        default:
          break
      }
    }
  })

  return (
    <div className="position-relative">
      <div className="d-inline-flex align-items-center flex-wrap p-1 form-control">
        {selectedItems.map((selectedItem, index) => {
          return (
            <span
              key={`selected-item-${index}`}
              className="px-1 mx-1 mb-1 rounded focus-ring"
              style={{
                background: 'rgb(226, 227, 229',
                whiteSpace: 'normal'
              }}
              {...getSelectedItemProps({ selectedItem, index })}
            >
              {itemToString(selectedItem)}
              <span
                className="px-1 cursor-pointer"
                onClick={e => {
                  e.stopPropagation()
                  removeSelectedItem(selectedItem)
                }}
              >
                <FontAwesomeIcon icon={faTimes} />
              </span>
            </span>
          )
        })}
        <div className="d-flex flex-grow-1">
          <input
            className="w-100"
            placeholder={placeholder}
            style={{ border: 'none' }}
            {...getInputProps(getDropdownProps({ preventKeyAction: isOpen }))}
          />
          <button
            aria-label="toggle menu"
            className="btn btn-light px-2 ms-1"
            {...getToggleButtonProps()}
          >
            <FontAwesomeIcon icon={faChevronDown} />
          </button>
        </div>
      </div>
      <ul
        className={classNames('list-unstyled m-0 p-0 bg-white', {
          invisible: !(isOpen && items.length)
        })}
        style={{
          position: 'absolute',
          overflowY: 'scroll',
          width: '100%',
          height:
            items.length <= 4
              ? itemHeights.slice(0, 4).reduce((a, b) => a + b, 0)
              : 250,
          whiteSpace: 'normal',
          zIndex: 99
        }}
        {...getMenuProps({ ref: listRef })}
      >
        {isOpen && (
          <>
            {/* Placeholder to make the list scrollable. */}
            <li
              key="total-size"
              ref={(el: HTMLLIElement) => {
                if (el) {
                  setItemWidth(el.getBoundingClientRect().width)
                }
              }}
              style={{ height: virtualizer.getTotalSize() }}
            />
            {/* Actual list items. */}
            {virtualizer.getVirtualItems().map(virtualRow => {
              const item = items[virtualRow.index]
              const label = itemToString(item)

              return (
                <li
                  key={virtualRow.key}
                  className="py-2 px-3"
                  style={{
                    position: 'absolute',
                    top: 0,
                    left: 0,
                    width: '100%',
                    height: virtualRow.size,
                    transform: `translateY(${virtualRow.start}px)`,
                    background:
                      highlightedIndex === virtualRow.index
                        ? 'rgb(207, 244, 252)'
                        : undefined
                  }}
                  {...getItemProps({ index: virtualRow.index, item })}
                >
                  {label}
                </li>
              )
            })}
          </>
        )}
      </ul>
    </div>
  )
}
