import { Dispatch, ReactNode, SetStateAction, useId } from 'react'
import { MultiValue } from 'react-select'

/**
 * helper function for setting up an accessible react-select component, returns the currently selected item, and a
 * set of params to pass into the select component
 *
 * onChange: pass to the onChange of the <Select>
 * options: pass to the options of the <Select>
 * selectedItem: current value, use in your component logic
 * selectedOption: pass to the "value" of the <Select>
 * selectInputId: pass ot the of the <Select>
 *
 * */
export default function useReactSingleSelect<T>(items: T[],
  labelFunction: (i: T) => {label: React.ReactNode, value: T},
  setSelectedItem: Dispatch<SetStateAction<T | undefined>> | ((x: T | undefined) => void), selectedItem?: T) {
  const options = items.map(labelFunction)
  const selectedValue = selectedItem ? labelFunction(selectedItem).value : undefined
  const selectedOption = options.find(opt => opt.value === selectedValue)
  const selectInputId = useId()

  const onChange = (opt: {label: React.ReactNode, value: T} | null) => setSelectedItem(opt?.value)
  return { onChange, options, selectedOption, selectInputId }
}

/**
 * helper function for setting up an accessible react-select component, returns the currently selected item, and a
 * set of params to pass into the select component
 *
 * onChange: pass to the onChange of the <Select>
 * options: pass to the options of the <Select>
 * selectedItem: current value, use in your component logic
 * selectedOption: pass to the "value" of the <Select>
 * selectInputId: pass ot the of the <Select>
 *
 * */
export function useNonNullReactSingleSelect<T>(items: T[],
  labelFunction: (i: T) => {label: React.ReactNode, value: T},
  setSelectedItem: Dispatch<SetStateAction<T>> | ((x: T) => void), selectedItem: T) {
  const options = items.map(labelFunction)
  const selectedValue = selectedItem ? labelFunction(selectedItem).value : undefined
  const selectedOption = options.find(opt => opt.value === selectedValue)
  const selectInputId = useId()

  const onChange = (opt: {label: React.ReactNode, value: T} | null) => setSelectedItem(opt?.value ?? selectedItem)
  return { onChange, options, selectedOption, selectInputId }
}

/**
 * helper function for setting up an accessible react-select component, returns the currently selected item, and a
 * set of params to pass into the select component
 *
 * onChange: pass to the onChange of the <Select>
 * options: pass to the options of the <Select>
 * selectedItem: current value, use in your component logic
 * selectedOption: pass to the "value" of the <Select>
 * selectInputId: pass ot the of the <Select>
 *
 * */
export function useReactMultiSelect<T>(items: T[],
  labelFunction: (i: T) => {label: React.ReactNode, value: T},
  setSelectedItems: Dispatch<SetStateAction<T[]>> | ((x: T[]) => void), selectedItems?: T[]) {
  const options = items.map(labelFunction)
  const selectedValues = selectedItems ?? []
  const selectedOptions = selectedValues.map(
    value => options.find(opt => opt.value === value))
  const selectInputId = useId()

  const onChange: ((opts: MultiValue<{ label: ReactNode, value: T } | undefined>) => void)
    = (opts: MultiValue<{label: ReactNode, value: T} | undefined>) =>
      setSelectedItems(opts.map(opt => opt ? opt.value : undefined)
        .filter(opt => !!opt) as T[])
  return { onChange, options, selectedOptions, selectInputId }
}
