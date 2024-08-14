import argparse
import csv
import os.path
from copy import deepcopy
from typing import Any, Union

from openpyxl import load_workbook


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
    parser.add_argument('-DDj', '--juniper-data-dict', required=True, help='Data diction from Juniper')
    parser.add_argument('-T', '--translation-override',
                        help='If there are any issues with automatic translation, you can '
                             'provide an override as a CSV file with two columns: <dsm '
                             'question id>,<juniper question id>')
    parser.add_argument('-I', '--in-file', required=True)
    parser.add_argument('-O', '--out-file', required=True)

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

    print(len(dsm_questions))
    print(len(leftover_dsm_questions))

    for t in translations:
        print(t.dsm_question_definition.stable_id + ' -> ' + t.juniper_question_definition.stable_id)
    # 4: alert user of discrepancies
    #    - if there are any DSM or juniper variables that couldn't be mapped, alert the user
    #    - they can accept this discrepancy or cancel out & fix it in translation override

    validate_leftover_questions(leftover_dsm_questions, leftover_juniper_questions)
    #
    # # 5: translate data
    # #    - parse the data & actually do the translation
    #
    # dsm_data = parse_dsm_data(args.in_file)
    # juniper_data = apply_translations(dsm_data, translations)
    #
    # write_data(args.out_file, juniper_data)


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
        survey_stable_id = current_survey
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

        # todo: handle subquestions of composite/dynamic panels,
        #       possibly in outer juniper/pepper specific function

        row_idx += 1
        num_blanks = 0

    return out


def parse_dsm_data_dict(filepath: str) -> list[DataDefinition]:
    # todo: add DSM-specific parsing logic after simple_parse_data_dict

    # todo: if question has [[]] around it, it's a composite/multi question
    #       - group subquestions together by looking at what questions
    #         start with the parent's stableid
    #       - if the data dict description doesn't have "May have up to <?> responses", then it's not actually
    #         a dynamicpanel question on the juniper side - we can just treat them like regular questions

    simple_questions = simple_parse_data_dict(filepath)

    questions = []

    while len(simple_questions) > 0:
        question = simple_questions.pop(0)
        if question.stable_id.startswith("[[") and question.stable_id.endswith("]]"):
            # these questions are either composite or multiselect
            # either way, we need to group their subquestions together
            question.stable_id = question.stable_id[2:-2] # remove the [[]]
            subquestions = list(filter(lambda q: q.stable_id.startswith(question.stable_id), simple_questions))

            # if the description doesn't have "May have up to <?> responses", then it's not a dynamicpanel
            if question.question_type.lower() == 'composite':
                if 'May have up to' not in question.description:
                    parent_question_id = question.stable_id.split('.')[-1]
                    # treat as regular questions
                    for subquestion in subquestions:
                        # subquestion.stable_id = subquestion.stable_id
                        questions.append(subquestion)
                    continue
                else:
                    # treat as dynamicpanel
                    question.subquestions = subquestions
                    questions.append(question)
            elif question.question_type.lower() == 'multiselect':
                # treat as multiselect
                question.subquestions = subquestions
                questions.append(question)
            else:
                print('Error: question ' + question.stable_id + ' is neither composite nor multiselect')
                exit(1)
            # remove all subquestions from the list
            simple_questions = list(filter(lambda q: q not in subquestions, simple_questions))
        else:
            questions.append(question)
    return questions


def parse_juniper_data_dict(filepath: str) -> list[DataDefinition]:
    # todo: add Juniper-specific parsing logic after simple_parse_data_dict
    simple_questions = simple_parse_data_dict(filepath)

    questions = []

    while len(simple_questions) > 0:
        question = simple_questions.pop(0)
        # subquestion; handled when we encounter the parent question
        if question.stable_id.endswith('[0]'):
            continue
        if question.question_type == 'paneldynamic':
            question.subquestions = []
            for subquestion in simple_questions:
                if subquestion.stable_id.startswith(question.stable_id):
                    simple_questions.remove(subquestion)

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


def parse_translation_override(filepath: str) -> list[TranslationOverride]:
    return []


class Translation:
    dsm_question_definition = None
    juniper_question_definition = None

    translation_override = None  #handles overrides of default behavior

    def __init__(self, dsm_question_definition: DataDefinition, juniper_question_definition: DataDefinition,
                 translation_override: TranslationOverride | None = None):
        self.dsm_question_definition = dsm_question_definition
        self.juniper_question_definition = juniper_question_definition
        self.translation_override = translation_override


default_translation_overrides = [
    # todo: email
    # todo: username
    # todo: profile info (name)
]

def create_translations(
        dsm_questions: list[DataDefinition],
        juniper_questions: list[DataDefinition],
        translation_overrides: list[TranslationOverride]
) -> tuple[list[DataDefinition], list[DataDefinition], list[Translation]]:
    # create constant default translations, e.g.:
    # - profile.email -> profile.contactEmail
    # - profile.email -> account.username

    leftover_dsm_questions = deepcopy(dsm_questions)
    leftover_juniper_questions = deepcopy(juniper_questions)

    translations = []

    for override in translation_overrides + default_translation_overrides:
        dsm_question = next((q for q in dsm_questions if q.stable_id == override.dsm_stable_id), None)
        juniper_question = next((q for q in juniper_questions if q.stable_id == override.juniper_stable_id), None)

        if dsm_question is not None and juniper_question is not None:
            translations.append(Translation(dsm_question, juniper_question, override))
            if dsm_question in leftover_dsm_questions:
                leftover_dsm_questions.remove(dsm_question)
            if juniper_question in leftover_juniper_questions:
                leftover_juniper_questions.remove(juniper_question)
        else:
            print('Error parsing translation override: ')
            if dsm_question is None:
                print('DSM question with stable ID ' + override.dsm_stable_id + ' not found')
            if juniper_question is None:
                print('Juniper question with stable ID ' + override.juniper_stable_id + ' not found')
            exit(1)

    # if not found in overrides, try to match by stable ID
    for dsm_question in leftover_dsm_questions:
        for juniper_question in leftover_juniper_questions:
            if is_matched(juniper_question, dsm_question):
                translations.append(Translation(dsm_question, juniper_question))
                # todo: if there is a composite question, make sure subquestions match
                leftover_dsm_questions.remove(dsm_question)
                leftover_juniper_questions.remove(juniper_question)
                break

    return leftover_dsm_questions, leftover_juniper_questions, translations


def is_matched(q1: DataDefinition, q2: DataDefinition) -> bool:
    split_q1 = q1.stable_id.split('.')
    split_q2 = q2.stable_id.split('.')

    # remove the survey prefix
    return '_'.join(split_q1[1:]) == '_'.join(split_q2[1:])


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

    return []


def apply_translations(data: list[dict[str, Any]], translations: list[Translation]) -> list[dict[str, Any]]:
    out: list[dict[str, Any]] = []

    for row in data:
        new_row = {}
        for translation in translations:
            apply_translation(row, new_row, translation)

        out.append(new_row)
    return []


def apply_translation(dsm_data: dict[str, Any], juniper_data: dict[str, Any], translation: Translation):
    # todo: convert DSM multiselect (multi-answer) to Juniper multiselect (single json answer)
    # todo: convert DSM composite question (multi-answer) to Juniper dynamic panel (single json answer)

    juniper_stable_id = translation.juniper_question_definition.stable_id
    dsm_stable_id = translation.dsm_question_definition.stable_id

    juniper_data[juniper_stable_id] = dsm_data[dsm_stable_id]


def write_data(outfile: str, data: list[dict[str, Any]]):
    with open(outfile, 'w') as f:
        writer = csv.writer(f)
        writer.writerow(data[0].keys())
        for row in data:
            writer.writerow(row.values())


if __name__ == '__main__':
    main()
