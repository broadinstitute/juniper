// Generated from src/main/antlr/bio/terra/pearl/core/antlr/CohortRule.g4 by ANTLR 4.13.1
// noinspection ES6UnusedImports,JSUnusedGlobalSymbols,JSUnusedLocalSymbols
import {
	ATN,
	ATNDeserializer,
	CharStream,
	DecisionState, DFA,
	Lexer,
	LexerATNSimulator,
	RuleContext,
	PredictionContextCache,
	Token
} from "antlr4";
export default class CohortRuleLexer extends Lexer {
	public static readonly NUMBER = 1;
	public static readonly STRING = 2;
	public static readonly VARIABLE = 3;
	public static readonly BOOLEAN = 4;
	public static readonly NULL = 5;
	public static readonly WS = 6;
	public static readonly OPERATOR = 7;
	public static readonly AND = 8;
	public static readonly OR = 9;
	public static readonly PAR_OPEN = 10;
	public static readonly PAR_CLOSE = 11;
	public static readonly EOF = Token.EOF;

	public static readonly channelNames: string[] = [ "DEFAULT_TOKEN_CHANNEL", "HIDDEN" ];
	public static readonly literalNames: (string | null)[] = [ null, null, 
                                                            null, null, 
                                                            null, "'null'", 
                                                            null, null, 
                                                            "'and'", "'or'", 
                                                            "'('", "')'" ];
	public static readonly symbolicNames: (string | null)[] = [ null, "NUMBER", 
                                                             "STRING", "VARIABLE", 
                                                             "BOOLEAN", 
                                                             "NULL", "WS", 
                                                             "OPERATOR", 
                                                             "AND", "OR", 
                                                             "PAR_OPEN", 
                                                             "PAR_CLOSE" ];
	public static readonly modeNames: string[] = [ "DEFAULT_MODE", ];

	public static readonly ruleNames: string[] = [
		"NUMBER", "STRING", "VARIABLE", "BOOLEAN", "NULL", "WS", "OPERATOR", "AND", 
		"OR", "PAR_OPEN", "PAR_CLOSE",
	];


	constructor(input: CharStream) {
		super(input);
		this._interp = new LexerATNSimulator(this, CohortRuleLexer._ATN, CohortRuleLexer.DecisionsToDFA, new PredictionContextCache());
	}

	public get grammarFileName(): string { return "CohortRule.g4"; }

	public get literalNames(): (string | null)[] { return CohortRuleLexer.literalNames; }
	public get symbolicNames(): (string | null)[] { return CohortRuleLexer.symbolicNames; }
	public get ruleNames(): string[] { return CohortRuleLexer.ruleNames; }

	public get serializedATN(): number[] { return CohortRuleLexer._serializedATN; }

	public get channelNames(): string[] { return CohortRuleLexer.channelNames; }

	public get modeNames(): string[] { return CohortRuleLexer.modeNames; }

	public static readonly _serializedATN: number[] = [4,0,11,105,6,-1,2,0,
	7,0,2,1,7,1,2,2,7,2,2,3,7,3,2,4,7,4,2,5,7,5,2,6,7,6,2,7,7,7,2,8,7,8,2,9,
	7,9,2,10,7,10,1,0,4,0,25,8,0,11,0,12,0,26,1,0,1,0,4,0,31,8,0,11,0,12,0,
	32,3,0,35,8,0,1,1,1,1,5,1,39,8,1,10,1,12,1,42,9,1,1,1,1,1,1,2,1,2,4,2,48,
	8,2,11,2,12,2,49,1,2,1,2,1,3,1,3,1,3,1,3,1,3,1,3,1,3,1,3,1,3,3,3,63,8,3,
	1,4,1,4,1,4,1,4,1,4,1,5,4,5,71,8,5,11,5,12,5,72,1,5,1,5,1,6,1,6,1,6,1,6,
	1,6,1,6,1,6,1,6,1,6,1,6,1,6,1,6,1,6,1,6,1,6,1,6,3,6,93,8,6,1,7,1,7,1,7,
	1,7,1,8,1,8,1,8,1,9,1,9,1,10,1,10,0,0,11,1,1,3,2,5,3,7,4,9,5,11,6,13,7,
	15,8,17,9,19,10,21,11,1,0,5,1,0,48,57,4,0,10,10,13,13,39,39,92,92,5,0,46,
	46,48,57,65,90,95,95,97,122,3,0,9,10,13,13,32,32,2,0,60,60,62,62,116,0,
	1,1,0,0,0,0,3,1,0,0,0,0,5,1,0,0,0,0,7,1,0,0,0,0,9,1,0,0,0,0,11,1,0,0,0,
	0,13,1,0,0,0,0,15,1,0,0,0,0,17,1,0,0,0,0,19,1,0,0,0,0,21,1,0,0,0,1,24,1,
	0,0,0,3,36,1,0,0,0,5,45,1,0,0,0,7,62,1,0,0,0,9,64,1,0,0,0,11,70,1,0,0,0,
	13,92,1,0,0,0,15,94,1,0,0,0,17,98,1,0,0,0,19,101,1,0,0,0,21,103,1,0,0,0,
	23,25,7,0,0,0,24,23,1,0,0,0,25,26,1,0,0,0,26,24,1,0,0,0,26,27,1,0,0,0,27,
	34,1,0,0,0,28,30,5,46,0,0,29,31,7,0,0,0,30,29,1,0,0,0,31,32,1,0,0,0,32,
	30,1,0,0,0,32,33,1,0,0,0,33,35,1,0,0,0,34,28,1,0,0,0,34,35,1,0,0,0,35,2,
	1,0,0,0,36,40,5,39,0,0,37,39,8,1,0,0,38,37,1,0,0,0,39,42,1,0,0,0,40,38,
	1,0,0,0,40,41,1,0,0,0,41,43,1,0,0,0,42,40,1,0,0,0,43,44,5,39,0,0,44,4,1,
	0,0,0,45,47,5,123,0,0,46,48,7,2,0,0,47,46,1,0,0,0,48,49,1,0,0,0,49,47,1,
	0,0,0,49,50,1,0,0,0,50,51,1,0,0,0,51,52,5,125,0,0,52,6,1,0,0,0,53,54,5,
	116,0,0,54,55,5,114,0,0,55,56,5,117,0,0,56,63,5,101,0,0,57,58,5,102,0,0,
	58,59,5,97,0,0,59,60,5,108,0,0,60,61,5,115,0,0,61,63,5,101,0,0,62,53,1,
	0,0,0,62,57,1,0,0,0,63,8,1,0,0,0,64,65,5,110,0,0,65,66,5,117,0,0,66,67,
	5,108,0,0,67,68,5,108,0,0,68,10,1,0,0,0,69,71,7,3,0,0,70,69,1,0,0,0,71,
	72,1,0,0,0,72,70,1,0,0,0,72,73,1,0,0,0,73,74,1,0,0,0,74,75,6,5,0,0,75,12,
	1,0,0,0,76,93,5,61,0,0,77,78,5,33,0,0,78,93,5,61,0,0,79,93,7,4,0,0,80,81,
	5,62,0,0,81,93,5,61,0,0,82,83,5,60,0,0,83,93,5,61,0,0,84,85,5,99,0,0,85,
	86,5,111,0,0,86,87,5,110,0,0,87,88,5,116,0,0,88,89,5,97,0,0,89,90,5,105,
	0,0,90,91,5,110,0,0,91,93,5,115,0,0,92,76,1,0,0,0,92,77,1,0,0,0,92,79,1,
	0,0,0,92,80,1,0,0,0,92,82,1,0,0,0,92,84,1,0,0,0,93,14,1,0,0,0,94,95,5,97,
	0,0,95,96,5,110,0,0,96,97,5,100,0,0,97,16,1,0,0,0,98,99,5,111,0,0,99,100,
	5,114,0,0,100,18,1,0,0,0,101,102,5,40,0,0,102,20,1,0,0,0,103,104,5,41,0,
	0,104,22,1,0,0,0,10,0,26,32,34,40,47,49,62,72,92,1,6,0,0];

	private static __ATN: ATN;
	public static get _ATN(): ATN {
		if (!CohortRuleLexer.__ATN) {
			CohortRuleLexer.__ATN = new ATNDeserializer().deserialize(CohortRuleLexer._serializedATN);
		}

		return CohortRuleLexer.__ATN;
	}


	static DecisionsToDFA = CohortRuleLexer._ATN.decisionToState.map( (ds: DecisionState, index: number) => new DFA(ds, index) );
}