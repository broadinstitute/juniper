import argparse
import os.path
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

    # 4: alert user of discrepancies
    #    - if there are any DSM or juniper variables that couldn't be mapped, alert the user
    #    - they can accept this discrepancy or cancel out & fix it in translation override

    validate_leftover_questions(leftover_dsm_questions, leftover_juniper_questions)

    # 5: translate data
    #    - parse the data & actually do the translation

    dsm_data = parse_dsm_data(args.in_file)
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
    survey_stable_id = None
    data_type = None
    format = None  # e.g., if date
    option_values = None  # list of values, no label

    num_repeats = None
    subquestions = None  # list of composite subquestions


def parse_dsm_data_dict(filepath: str) -> list[DataDefinition]:
    dsm_data_dict = load_workbook(filename=filepath)

    # steps:
    # - iterate through leftmost (A) column; if column above is empty, then it's the start of a new survey
    #   - skip first survey line: it's just header
    #   - import question
    #      - if type is empty, and C (question type) is composite,
    #        remove sub-questions and put them into data definition
    #      - otherwise, A = question name B = type E = choices

    return []


def parse_juniper_data_dict(filepath: str) -> list[DataDefinition]:

    # steps:
    # - iterate through leftmost (A) column; if column above is empty, then it's the start of a new survey
    #   - skip first survey line: it's just header
    #   - import question
    #      - if type is empty, and C (question type) is composite,
    #        remove sub-questions and put them into data definition
    #      - otherwise, A = question name B = type E = choices

    return []


class TranslationOverride:
    dsm_stable_id = None
    juniper_stable_id = None


def parse_translation_override(filepath: str) -> list[TranslationOverride]:
    return []


class Translation:
    dsm_question_definition = None
    juniper_question_definition = None


def create_translations(
        dsm_questions: list[DataDefinition],
        juniper_questions: list[DataDefinition],
        translation_overrides: list[TranslationOverride]
) -> tuple[list[DataDefinition], list[DataDefinition], list[Translation]]:
    # create constant default translations, e.g.:
    # - profile.email -> profile.contactEmail
    # - profile.email -> account.username

    return [], [], []


def validate_leftover_questions(
        leftover_dsm_questions: list[DataDefinition],
        leftover_juniper_questions: list[DataDefinition]
):
    # todo: print out every leftover question

    if len(leftover_juniper_questions) > 0 or len(leftover_dsm_questions) > 0:
        # todo: print out possible solutions

        confirmation = input('Is this OK? (y/n): ')
        if not confirmation.lower().startswith('y'):
            exit(1)


def parse_dsm_data(filepath: str) -> list[dict[str, Any]]:
    return []


def apply_translations(data: list[dict[str, Any]], translations: list[Translation]) -> list[dict[str, Any]]:
    return []


def write_data(outfile: str, data: list[dict[str, Any]]):
    pass


if __name__ == '__main__':
    main()
