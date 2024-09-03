import argparse
import csv
import os.path
import re
from copy import copy
from datetime import datetime
from typing import Any, Union

from openpyxl import load_workbook


# todo s:
#       - figure out _DETAIL questions on the import side
#       - check multi-select options match?
#       - for each survey, also map `.COMPLETEDAT` to `lastUpdatedAt` and `completed` on the juniper side
#       - create export rows for non-subject users that are proxies of a governed user
#       - export enrollee.subject=true for all subjects (how to track?)

# Usage:
# python translate.py -DDd <dsm_data_dict> -DDj <juniper_data_dict> -T <translation_override> -I <in_file> -O <out_file>
# Translates data from Pepper into a format that you can use as the basis for importing participants in Juniper.
#
# Step 1: Download a data export from DSM with all the data that you need
# Step 2: Download the Juniper data dictionary in your recreated study
# Step 3: Run the script with the following arguments:
# python translate.py -DDd <dsm_data_dict> -DDj <juniper_data_dict> -I <in_file> -O <out_file>
# Step 4: If any questions are not matched, the script will alert you. You can either accept the discrepancy or
#         cancel out and fix it by creating a translation override file.
#         Example:
#         profile.email,profile.contactEmail
#         profile.email,account.username
#         ...
# Step 5: Run the script again with the translation override file:
# python translate.py -DDd <dsm_data_dict> -DDj <juniper_data_dict> -T <translation_override> -I <in_file> -O <out_file>
# Step 6: Use the generated CSV file to import the data into Juniper


def main():
    # 1: parse arguments
    #    - --dsm-data-dict (-DDd)
    #    - --juniper-data-dict (-DDj)
    #    - --translation-override (-T)
    #    - --in-dsm-data (-I)
    #    - --out-file (-O)

    parser = argparse.ArgumentParser(
        prog='DSM->Juniper Data Translation Script',
        description='Converts data from DSM to Juniper.')

    parser.add_argument('-DDd', '--dsm-data-dict', required=True, help='Data dictionary from DSM export')
    parser.add_argument('-DDj', '--juniper-data-dict', required=True, help='Data dictionary from Juniper')
    parser.add_argument('-T', '--translation-override',
                        help='If there are any issues with automatic translation, you can '
                             'provide an override as a CSV file with two columns: <dsm '
                             'question id>,<juniper question id>')
    parser.add_argument('-I', '--in-file', required=True)
    parser.add_argument('-O', '--out-file', required=True)
    parser.add_argument('-L', '--limit', type=int, default=None)

    args = parser.parse_args()

    ensure_files_exist([args.dsm_data_dict, args.juniper_data_dict, args.in_file, args.translation_override])

    # 2: parse data dictionaries
    #    - create list of dsm questions with metadata (survey, etc.)
    #    - create list of juniper questions with metadata (survey, etc.)

    dsm_questions: list[DataDefinition] = parse_dsm_data_dict(args.dsm_data_dict)
    juniper_questions: list[DataDefinition] = parse_juniper_data_dict(args.juniper_data_dict)

    # 3: create translation object
    #    - parse translation override and create initial translations from it
    #    - auto-create rest of the translations

    translation_overrides: list[TranslationOverride] = parse_translation_override(args.translation_override)

    (
        leftover_dsm_questions,
        leftover_juniper_questions,
        translations
    ) = create_translations(dsm_questions, juniper_questions, translation_overrides)

    print('Translations')
    for t in translations:
        print_translation(t)
    print()

    # 4: alert user of discrepancies
    #    - if there are any DSM or juniper variables that couldn't be mapped, alert the user
    #    - they can accept this discrepancy or cancel out & fix it in translation override

    validate_leftover_questions(leftover_dsm_questions, leftover_juniper_questions)

    # 5: translate data
    #    - parse the data & actually do the translation

    dsm_data = parse_dsm_data(args.in_file)

    if args.limit is not None:
        dsm_data = dsm_data[:args.limit]

    juniper_data = apply_translations(dsm_data, translations)

    write_data(args.out_file, juniper_data)


# ------ helper classes and methods --------

def ensure_files_exist(files: list[Union[str, None]]):
    for file in files:
        if file is None or file == "":
            continue
        if not os.path.isfile(file):
            print('File "' + file + '" does not exist.')
            exit(1)


class DataDefinition:
    stable_id = None
    data_type = None
    question_type = None
    format = None  # e.g., if date
    option_values = None  # list of values, no label
    description = None

    num_repeats = None
    subquestions = None  # list of composite subquestions

    def __init__(self,
                 stable_id: str,
                 data_type: str,
                 description: str,
                 question_type: str,
                 format: str | None = None,
                 option_values: list[str] | None = None,
                 num_repeats: int | None = None,
                 subquestions: list[Any] | None = None):
        self.stable_id = stable_id
        self.data_type = data_type
        self.description = description
        self.question_type = question_type
        self.format = format
        self.option_values = option_values

        self.num_repeats = num_repeats
        self.subquestions = subquestions


def print_translation(translation, prefix: str = ''):
    prefix = prefix or ''

    dsm_stable_id = str(translation.dsm_question_definition.stable_id) if translation.dsm_question_definition else ''
    juniper_stable_id = str(translation.juniper_question_definition.stable_id)

    if translation.translation_override is not None and translation.translation_override.constant_value is not None:
        print(prefix + juniper_stable_id + ' (constant value: ' + translation.translation_override.constant_value + ')')
    else:
        print(prefix + dsm_stable_id + ' -> ' + juniper_stable_id)

    for sub in translation.subquestion_translations:
        print_translation(sub, prefix + '\t')


def simple_parse_data_dict(filepath: str) -> list[DataDefinition]:
    dsm_data_dict = load_workbook(filename=filepath)
    dsm_data_dict = dsm_data_dict.worksheets[0]

    # steps:
    # - iterate through leftmost (A) column; if column above is empty, then it's the start of a new survey
    #   - skip first survey line: it's just header
    #   - import questions until blank line

    row_idx = 1
    num_blanks = 0
    out: list[DataDefinition] = []
    current_survey = ""
    while True:
        name_col = str(dsm_data_dict['A' + str(row_idx)].value or '')
        if name_col is None or name_col == '':
            if num_blanks == 0:
                # survey is done
                current_survey = ""
            num_blanks += 1
            if num_blanks > 3:
                break
            row_idx += 1
            continue

        if num_blanks != 0:
            current_survey = name_col
            # skip header
            row_idx += 2
            num_blanks = 0
            continue

        stable_id = name_col

        data_type = str(dsm_data_dict['B' + str(row_idx)].value or '')
        question_type = str(dsm_data_dict['C' + str(row_idx)].value or '')
        description = str(dsm_data_dict['D' + str(row_idx)].value or '')
        options = str(dsm_data_dict['E' + str(row_idx)].value or '')

        option_values = None
        if options != '':
            option_texts = options.split('\n')
            option_values = [option.split(' ')[0] for option in option_texts]

        question: DataDefinition = DataDefinition(
            stable_id, data_type, description, question_type, option_values=option_values
        )
        out.append(question)

        row_idx += 1
        num_blanks = 0

    return out


def parse_dsm_data_dict(filepath: str) -> list[DataDefinition]:
    simple_questions = simple_parse_data_dict(filepath)

    questions = []

    while len(simple_questions) > 0:
        question = simple_questions.pop(0)
        if question.stable_id.startswith("[[") and question.stable_id.endswith("]]"):
            # these questions are either composite or multiselect
            # either way, we need to group their subquestions together
            question.stable_id = question.stable_id[2:-2]  # remove the [[]]
            subquestions = list(
                filter(lambda q: q.stable_id.startswith(question.stable_id) and not q.stable_id.endswith('_DETAIL'),
                       simple_questions))

            # if the description doesn't have "May have up to <?> responses", then it's not a dynamicpanel
            if question.question_type.lower() == 'composite':
                # if not, then it's a regular panel, so we should just ignore it.
                if 'May have up to' in question.description:
                    # treat as dynamicpanel
                    question.subquestions = subquestions
                    questions.append(question)
            elif question.question_type.lower() == 'multiselect':
                # treat as multiselect
                question.subquestions = subquestions
                questions.append(question)

                for subquestion in subquestions:
                    simple_questions.remove(subquestion)
            else:
                print('Error: question ' + question.stable_id + ' is neither composite nor multiselect')
                exit(1)
            # remove all subquestions from the list
            # let's not so we can keep track of if they were matched
            # simple_questions = list(filter(lambda q: q not in subquestions, simple_questions))
        else:
            questions.append(question)
    return questions


def parse_juniper_data_dict(filepath: str) -> list[DataDefinition]:
    simple_questions = simple_parse_data_dict(filepath)

    questions = []

    while len(simple_questions) > 0:
        question = simple_questions.pop(0)
        # subquestion; handled when we encounter the parent question
        if question.stable_id.endswith('[0]'):
            continue
        if question.option_values == ['0']:
            # these are derived columns for muliselect
            # questions; we handle these when we encounter
            # the parent question, so we can ignore these
            continue
        if question.question_type == 'paneldynamic':
            question.subquestions = []
            for subquestion in simple_questions:
                if subquestion.stable_id.startswith(question.stable_id):
                    # let's not do this, so we can keep track of if they have been matched
                    # simple_questions.remove(subquestion)

                    # remove the index from the stableid
                    subquestion.stable_id = subquestion.stable_id[:-3]
                    question.subquestions.append(subquestion)

            questions.append(question)
        else:
            questions.append(question)

    return questions


class TranslationOverride:
    dsm_stable_id = None
    juniper_stable_id = None

    constant_value = None
    value_if_present = None

    def __init__(self, dsm_stable_id: str | None, juniper_stable_id: str | None, constant_value=None,
                 value_if_present=None):
        self.dsm_stable_id = dsm_stable_id
        self.juniper_stable_id = juniper_stable_id
        self.constant_value = constant_value
        self.value_if_present = value_if_present


def parse_translation_override(filepath: str) -> list[TranslationOverride]:
    if filepath is None or filepath == '':
        return []

    out: list[TranslationOverride] = []

    with open(filepath, 'r') as f:
        for row in csv.reader(f):
            out.append(TranslationOverride(row[0], row[1]))

    return out


class Translation:
    dsm_question_definition = None
    juniper_question_definition = None

    subquestion_translations = []

    translation_override = None  # handles overrides of default behavior

    def __init__(self, dsm_question_definition: DataDefinition, juniper_question_definition: DataDefinition,
                 translation_override: TranslationOverride | None = None,
                 subquestion_translations=None):
        self.dsm_question_definition = dsm_question_definition
        self.juniper_question_definition = juniper_question_definition
        self.translation_override = translation_override
        self.subquestion_translations = subquestion_translations or []


default_translation_overrides = [
    TranslationOverride('PROFILE.EMAIL', 'profile.contactEmail'),
    TranslationOverride('PROFILE.EMAIL', 'account.username'),
    # if the user doesn't have an email, it usually means their proxy does, so check there
    # as well
    TranslationOverride('PROFILE.PROXY.EMAIL', 'account.username'),
    TranslationOverride('PROFILE.FIRSTNAME', 'profile.givenName'),
    TranslationOverride('PROFILE.LASTNAME', 'profile.familyName'),
    TranslationOverride(None, 'enrollee.subject', constant_value='true'),
    TranslationOverride('CONSENT.CREATEDAT', 'enrollee.role', value_if_present='true'),
]


def create_translations(
        dsm_questions: list[DataDefinition],
        juniper_questions: list[DataDefinition],
        translation_overrides: list[TranslationOverride]
) -> tuple[list[DataDefinition], list[DataDefinition], list[Translation]]:
    # create constant default translations, e.g.:
    # - profile.email -> profile.contactEmail
    # - profile.email -> account.username

    all_dsm_questions = copy(dsm_questions)
    all_juniper_questions = copy(juniper_questions)

    dsm_questions = copy(dsm_questions)
    juniper_questions = copy(juniper_questions)

    unmatched_dsm_questions = []

    translations = []

    for override in translation_overrides + default_translation_overrides:
        dsm_question = next((q for q in all_dsm_questions if q.stable_id == override.dsm_stable_id), None)
        juniper_question = next((q for q in all_juniper_questions if q.stable_id == override.juniper_stable_id), None)

        if (override.juniper_stable_id == '' or juniper_question is not None) and (
                override.dsm_stable_id == '' or dsm_question is not None):
            translations.append(Translation(dsm_question, juniper_question, override))
            # remove from lists; we don't need to match these anymore
            if dsm_question in dsm_questions:
                dsm_questions.remove(dsm_question)

            if juniper_question in juniper_questions:
                juniper_questions.remove(juniper_question)
        else:
            print('Error parsing translation override: ')
            if dsm_question is None and override.dsm_stable_id is not None:
                print('DSM question with stable ID ' + override.dsm_stable_id + ' not found')
            if juniper_question is None:
                print('Juniper question with stable ID ' + override.juniper_stable_id + ' not found')

    # if not found in overrides, try to match by stable ID
    while len(dsm_questions) > 0 and len(juniper_questions) > 0:
        dsm_question = dsm_questions.pop(0)
        matched_juniper_question = next((q for q in juniper_questions if is_matched(dsm_question, q)), None)

        if matched_juniper_question is not None:
            translations.append(Translation(dsm_question, matched_juniper_question))
            juniper_questions.remove(matched_juniper_question)

            if matched_juniper_question.question_type == 'paneldynamic':
                for dsm_subquestion in dsm_question.subquestions:
                    juniper_subquestion = next((q for q in juniper_questions if is_matched(dsm_subquestion, q)), None)
                    if juniper_subquestion is not None:
                        translations[-1].subquestion_translations.append(
                            Translation(dsm_subquestion, juniper_subquestion)
                        )
                        juniper_questions.remove(juniper_subquestion)
                        dsm_questions.remove(dsm_subquestion)

        else:
            unmatched_dsm_questions.append(dsm_question)

    create_workflow_translations(unmatched_dsm_questions, juniper_questions, translations)
    return unmatched_dsm_questions, juniper_questions, translations


def create_workflow_translations(
        dsm_questions: list[DataDefinition],
        juniper_questions: list[DataDefinition],
        translations: list[Translation]
):
    for dsm_question in dsm_questions:
        # look for known DSM questions, e.g. survey completion
        if dsm_question.stable_id.endswith(".LASTUPDATEDAT") and dsm_question.stable_id.count('.') == 1:
            survey_name = dsm_question.stable_id.split('.')[0]
            last_updated_at_juniper_question = next(
                (q for q in juniper_questions if q.stable_id == survey_name + '.lastUpdatedAt'), None)
            if last_updated_at_juniper_question is not None:
                translations.append(Translation(dsm_question, last_updated_at_juniper_question))
                juniper_questions.remove(last_updated_at_juniper_question)
                if dsm_question in dsm_questions:
                    dsm_questions.remove(dsm_question)
        if dsm_question.stable_id.endswith(".COMPLETEDAT") and dsm_question.stable_id.count('.') == 1:
            survey_name = dsm_question.stable_id.split('.')[0]
            completed_juniper_question = next(
                (q for q in juniper_questions if q.stable_id == survey_name + '.complete'), None)
            if completed_juniper_question is not None:
                translations.append(Translation(dsm_question, completed_juniper_question,
                                                translation_override=TranslationOverride(None, None,
                                                                                         value_if_present='true')))
                juniper_questions.remove(completed_juniper_question)
                if dsm_question in dsm_questions:
                    dsm_questions.remove(dsm_question)


def is_matched(q1: DataDefinition, q2: DataDefinition) -> bool:
    return standardize_stable_id(q1.stable_id) == standardize_stable_id(q2.stable_id)


def standardize_stable_id(stable_id: str) -> str:
    if stable_id.endswith('[0]'):
        stable_id = stable_id[:-3]

    return stable_id.replace('.', '_').strip().lower()


def validate_leftover_questions(
        leftover_dsm_questions: list[DataDefinition],
        leftover_juniper_questions: list[DataDefinition]
):
    if len(leftover_dsm_questions) == 0 and len(leftover_juniper_questions) == 0:
        return

    print('There are questions that could not be matched:')

    if len(leftover_dsm_questions) > 0:
        print('DSM questions:')
        for dsm_question in leftover_dsm_questions:
            print('\t' + dsm_question.stable_id)

    if len(leftover_juniper_questions) > 0:
        print('Juniper questions:')
        for juniper_question in leftover_juniper_questions:
            print('\t' + juniper_question.stable_id)

    print('If any of these questions need to be imported '
          'from DSM to Juniper, please add them to the '
          'translation override file.')

    confirmation = input('Is this OK? (y/n): ')
    if not confirmation.lower().startswith('y'):
        exit(1)


def parse_dsm_data(filepath: str) -> list[dict[str, Any]]:
    raw_data = []
    with open(filepath, 'r') as f:
        # pepper exports as tsv
        for row in csv.reader(f, delimiter="\t", quotechar='"'):
            raw_data.append(row)

    header = raw_data[0]

    data: list[dict[str, Any]] = []

    # start at 2 because row 0 contains headers and 1 contains labels
    for row in raw_data[2:]:
        new_row = {}
        for i, value in enumerate(row):
            new_row[header[i]] = value
        data.append(new_row)

    return data

necessary_columns = ['account.username', 'profile.birthDate']

def apply_translations(data: list[dict[str, Any]], translations: list[Translation]) -> list[dict[str, Any]]:
    out: list[dict[str, Any]] = []

    for row in data:
        new_row = {}
        for translation in translations:
            apply_translation(row, new_row, translation)

        has_all_needed_columns = True
        for column in necessary_columns:
            if column not in new_row or new_row[column] is None or new_row[column] == '' :
                print(f'Warning: skipping user with missing {column}')
                has_all_needed_columns = False

        if has_all_needed_columns:
            out.append(new_row)

    return out


def apply_translation(dsm_data: dict[str, Any], juniper_data: dict[str, Any], translation: Translation):
    if translation.translation_override is not None and translation.translation_override.constant_value is not None:
        juniper_data[
            translation.juniper_question_definition.stable_id
        ] = translation.translation_override.constant_value
        return

    juniper_question = translation.juniper_question_definition
    dsm_question = translation.dsm_question_definition

    if juniper_question.question_type == 'paneldynamic':
        # todo: make sure this is right; does it need to be parsed as a string?
        juniper_data[juniper_question.stable_id] = get_dynamic_panel_values(translation, dsm_data)
    elif dsm_question.question_type.lower() == 'multiselect':
        # todo: make sure this is correct, possibly convert to string?
        juniper_data[juniper_question.stable_id] = get_multi_panel_values(translation, dsm_data)
    else:
        simple_translate(
            translation, dsm_data, juniper_data
        )


def simple_translate(translation: Translation,
                     dsm_data: dict[str, Any],
                     juniper_data: dict[str, Any]) -> Any:
    juniper_question = translation.juniper_question_definition
    dsm_question = translation.dsm_question_definition

    values = get_all_values(dsm_question, dsm_data)

    for idx in range(len(values)):
        response_stable_id = get_juniper_response_stable_id(juniper_question, idx)
        value = values[idx]
        if (response_stable_id in juniper_data
                and juniper_data[response_stable_id] is not None
                and len(juniper_data[response_stable_id]) > 0):
            continue  # assume any value is good enough
        juniper_data[response_stable_id] = translate_value(translation, value)


def translate_value(translation: Translation, value: Any) -> Any:
    if translation.translation_override is not None and translation.translation_override.value_if_present is not None:
        return translation.translation_override.value_if_present if value.strip() != '' else None

    # possible data types: string, date, boolean, date_time, object_string

    if translation.juniper_question_definition.data_type in ['string', 'object_string']:
        return str(value)
    elif translation.juniper_question_definition.data_type == 'date':
        if translation.dsm_question_definition.data_type not in ['date', 'datetime']:
            print_wrong_type_warning(translation.dsm_question_definition, translation.juniper_question_definition)
        return convert_date(value)
    elif translation.juniper_question_definition.data_type == 'date_time':
        if translation.dsm_question_definition.data_type not in ['datetime', 'date']:
            print_wrong_type_warning(translation.dsm_question_definition, translation.juniper_question_definition)
        if translation.dsm_question_definition.data_type == 'date':
            return convert_date_to_date_time(value)
        return convert_date_time(value)
    elif translation.juniper_question_definition.data_type == 'boolean':
        # we'll assume that strings can be mapped to booleans...
        if translation.dsm_question_definition.data_type not in ['boolean', 'string']:
            print_wrong_type_warning(translation.dsm_question_definition, translation.juniper_question_definition)
        return convert_boolean(value)
    return value


def print_wrong_type_warning(dsm_question: DataDefinition, juniper_question: DataDefinition):
    print(f'Warning: translating DSM question ({dsm_question.stable_id}:{dsm_question.data_type}) '
          f'to Juniper question ({juniper_question.stable_id}:{juniper_question.data_type}) '
          f'with different data types')


def convert_date(value: str) -> str:
    return value


def convert_date_to_date_time(value: str) -> str:
    if value is None or value == '':
        return value or ''

    datetime_object = datetime.strptime(value, '%m-%d-%Y')

    return datetime_object.strftime('%Y-%m-%d %I:%M%p')


def convert_date_time_to_date(value: str) -> str:
    if value is None or value == '':
        return value or ''

    datetime_object = datetime.strptime(value, '%m-%d-%Y %H:%M:%S')

    return datetime_object.strftime('%Y-%m-%d')


def convert_date_time(value: str) -> str:
    if value is None or value == '':
        return value or ''

    datetime_object = datetime.strptime(value, '%m-%d-%Y %H:%M:%S')

    return datetime_object.strftime('%Y-%m-%d %I:%M%p')


def convert_boolean(value: str) -> bool:
    return value.lower() == 'true'


def get_all_values(dsm_question: DataDefinition, dsm_data: dict[str, Any]) -> list[str]:
    out = {0: dsm_data[dsm_question.stable_id]}

    for [key, value] in dsm_data.items():
        if key.startswith(dsm_question.stable_id) and is_repeat_question(dsm_question.stable_id, key):
            index = get_repeat_index(key)
            out[index] = value

    return [out[i] for i in range(len(out))]


def is_repeat_question(question_stable_id: str, response_stable_id: str) -> bool:
    # use regex to match the question stable ID
    # format: question_stable_id_response_stable_id_[0-9]+
    return re.match(question_stable_id + '_[0-9]+', response_stable_id) is not None


def get_juniper_response_stable_id(juniper_question: DataDefinition, repeat: int) -> str:
    stable_id = juniper_question.stable_id

    if repeat == 0:
        return stable_id

    [survey_id, question_id] = stable_id.partition('.')
    return survey_id + '.' + str(repeat) + '.' + question_id


def get_repeat_index(response_stable_id: str) -> int:
    return int(response_stable_id.split('_')[-1])


def get_dynamic_panel_values(translation: Translation, dsm_data: dict[str, Any]) -> list[dict[str, Any]]:
    out_value: list[dict[str, Any]] = []
    for subquestion_translation in translation.subquestion_translations:
        subquestion_values: list[tuple[str, Any]] = []  # find all DSM answers to this subquestion
        for key, value in dsm_data.items():
            if key.startswith(subquestion_translation.dsm_question_definition.stable_id):
                subquestion_values.append((key, value))

        for subquestion_stable_id, subquestion_data in subquestion_values:
            if subquestion_data.strip() == '':
                continue

            index = subquestion_stable_id.split('_')[-1]
            try:
                index = int(index)
            except:
                # pepper leaves out the index if it's 0
                index = 1

            juniper_idx = index - 1

            if len(out_value) < juniper_idx + 1:
                for i in range(len(out_value), juniper_idx + 1):
                    out_value.append({})

            out_value[
                juniper_idx
            ][
                strip_parent_stable_id(translation.juniper_question_definition.stable_id,
                                       subquestion_translation.juniper_question_definition.stable_id)
            ] = subquestion_data
    return out_value


def strip_parent_stable_id(parent_stable_id: str, subquestion_stable_id: str) -> str:
    return subquestion_stable_id[len(parent_stable_id) + 1:]


def get_multi_panel_values(translation: Translation, dsm_data: dict[str, Any]) -> list[str]:
    out_value = []
    for subquestion in translation.dsm_question_definition.subquestions:
        if dsm_data[subquestion.stable_id] == '1':
            option = subquestion.stable_id.split('.')[-1]  # todo make this is right
            out_value.append(option)
    return out_value


def write_data(outfile: str, data: list[dict[str, Any]]):
    with open(outfile, 'w') as f:
        writer = csv.writer(f)
        writer.writerow(data[0].keys())
        for row in data:
            writer.writerow(row.values())


if __name__ == '__main__':
    main()
