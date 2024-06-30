// Generated from bio/terra/pearl/core/antlr/CohortRule.g4 by ANTLR 4.13.1
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
	public static readonly T__0 = 1;
	public static readonly NUMBER = 2;
	public static readonly STRING = 3;
	public static readonly VARIABLE = 4;
	public static readonly BOOLEAN = 5;
	public static readonly NULL = 6;
	public static readonly WS = 7;
	public static readonly OPERATOR = 8;
	public static readonly AND = 9;
	public static readonly OR = 10;
	public static readonly PAR_OPEN = 11;
	public static readonly PAR_CLOSE = 12;
	public static readonly NOT = 13;
	public static readonly FUNCTION_NAME = 14;
	public static readonly EOF = Token.EOF;

	public static readonly channelNames: string[] = [ "DEFAULT_TOKEN_CHANNEL", "HIDDEN" ];
	public static readonly literalNames: (string | null)[] = [ null, "','", 
                                                            null, null, 
                                                            null, null, 
                                                            "'null'", null, 
                                                            null, "'and'", 
                                                            "'or'", "'('", 
                                                            "')'", "'!'" ];
	public static readonly symbolicNames: (string | null)[] = [ null, null, 
                                                             "NUMBER", "STRING", 
                                                             "VARIABLE", 
                                                             "BOOLEAN", 
                                                             "NULL", "WS", 
                                                             "OPERATOR", 
                                                             "AND", "OR", 
                                                             "PAR_OPEN", 
                                                             "PAR_CLOSE", 
                                                             "NOT", "FUNCTION_NAME" ];
	public static readonly modeNames: string[] = [ "DEFAULT_MODE", ];

	public static readonly ruleNames: string[] = [
		"T__0", "NUMBER", "STRING", "VARIABLE", "BOOLEAN", "NULL", "WS", "OPERATOR", 
		"AND", "OR", "PAR_OPEN", "PAR_CLOSE", "NOT", "FUNCTION_NAME",
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

	public static readonly _serializedATN: number[] = [4,0,14,120,6,-1,2,0,
	7,0,2,1,7,1,2,2,7,2,2,3,7,3,2,4,7,4,2,5,7,5,2,6,7,6,2,7,7,7,2,8,7,8,2,9,
	7,9,2,10,7,10,2,11,7,11,2,12,7,12,2,13,7,13,1,0,1,0,1,1,4,1,33,8,1,11,1,
	12,1,34,1,1,1,1,4,1,39,8,1,11,1,12,1,40,3,1,43,8,1,1,2,1,2,5,2,47,8,2,10,
	2,12,2,50,9,2,1,2,1,2,1,3,1,3,4,3,56,8,3,11,3,12,3,57,1,3,1,3,1,4,1,4,1,
	4,1,4,1,4,1,4,1,4,1,4,1,4,3,4,71,8,4,1,5,1,5,1,5,1,5,1,5,1,6,4,6,79,8,6,
	11,6,12,6,80,1,6,1,6,1,7,1,7,1,7,1,7,1,7,1,7,1,7,1,7,1,7,1,7,1,7,1,7,1,
	7,1,7,1,7,1,7,3,7,101,8,7,1,8,1,8,1,8,1,8,1,9,1,9,1,9,1,10,1,10,1,11,1,
	11,1,12,1,12,1,13,4,13,117,8,13,11,13,12,13,118,0,0,14,1,1,3,2,5,3,7,4,
	9,5,11,6,13,7,15,8,17,9,19,10,21,11,23,12,25,13,27,14,1,0,6,1,0,48,57,4,
	0,10,10,13,13,39,39,92,92,5,0,46,46,48,57,65,90,95,95,97,122,3,0,9,10,13,
	13,32,32,2,0,60,60,62,62,3,0,65,90,95,95,97,122,132,0,1,1,0,0,0,0,3,1,0,
	0,0,0,5,1,0,0,0,0,7,1,0,0,0,0,9,1,0,0,0,0,11,1,0,0,0,0,13,1,0,0,0,0,15,
	1,0,0,0,0,17,1,0,0,0,0,19,1,0,0,0,0,21,1,0,0,0,0,23,1,0,0,0,0,25,1,0,0,
	0,0,27,1,0,0,0,1,29,1,0,0,0,3,32,1,0,0,0,5,44,1,0,0,0,7,53,1,0,0,0,9,70,
	1,0,0,0,11,72,1,0,0,0,13,78,1,0,0,0,15,100,1,0,0,0,17,102,1,0,0,0,19,106,
	1,0,0,0,21,109,1,0,0,0,23,111,1,0,0,0,25,113,1,0,0,0,27,116,1,0,0,0,29,
	30,5,44,0,0,30,2,1,0,0,0,31,33,7,0,0,0,32,31,1,0,0,0,33,34,1,0,0,0,34,32,
	1,0,0,0,34,35,1,0,0,0,35,42,1,0,0,0,36,38,5,46,0,0,37,39,7,0,0,0,38,37,
	1,0,0,0,39,40,1,0,0,0,40,38,1,0,0,0,40,41,1,0,0,0,41,43,1,0,0,0,42,36,1,
	0,0,0,42,43,1,0,0,0,43,4,1,0,0,0,44,48,5,39,0,0,45,47,8,1,0,0,46,45,1,0,
	0,0,47,50,1,0,0,0,48,46,1,0,0,0,48,49,1,0,0,0,49,51,1,0,0,0,50,48,1,0,0,
	0,51,52,5,39,0,0,52,6,1,0,0,0,53,55,5,123,0,0,54,56,7,2,0,0,55,54,1,0,0,
	0,56,57,1,0,0,0,57,55,1,0,0,0,57,58,1,0,0,0,58,59,1,0,0,0,59,60,5,125,0,
	0,60,8,1,0,0,0,61,62,5,116,0,0,62,63,5,114,0,0,63,64,5,117,0,0,64,71,5,
	101,0,0,65,66,5,102,0,0,66,67,5,97,0,0,67,68,5,108,0,0,68,69,5,115,0,0,
	69,71,5,101,0,0,70,61,1,0,0,0,70,65,1,0,0,0,71,10,1,0,0,0,72,73,5,110,0,
	0,73,74,5,117,0,0,74,75,5,108,0,0,75,76,5,108,0,0,76,12,1,0,0,0,77,79,7,
	3,0,0,78,77,1,0,0,0,79,80,1,0,0,0,80,78,1,0,0,0,80,81,1,0,0,0,81,82,1,0,
	0,0,82,83,6,6,0,0,83,14,1,0,0,0,84,101,5,61,0,0,85,86,5,33,0,0,86,101,5,
	61,0,0,87,101,7,4,0,0,88,89,5,62,0,0,89,101,5,61,0,0,90,91,5,60,0,0,91,
	101,5,61,0,0,92,93,5,99,0,0,93,94,5,111,0,0,94,95,5,110,0,0,95,96,5,116,
	0,0,96,97,5,97,0,0,97,98,5,105,0,0,98,99,5,110,0,0,99,101,5,115,0,0,100,
	84,1,0,0,0,100,85,1,0,0,0,100,87,1,0,0,0,100,88,1,0,0,0,100,90,1,0,0,0,
	100,92,1,0,0,0,101,16,1,0,0,0,102,103,5,97,0,0,103,104,5,110,0,0,104,105,
	5,100,0,0,105,18,1,0,0,0,106,107,5,111,0,0,107,108,5,114,0,0,108,20,1,0,
	0,0,109,110,5,40,0,0,110,22,1,0,0,0,111,112,5,41,0,0,112,24,1,0,0,0,113,
	114,5,33,0,0,114,26,1,0,0,0,115,117,7,5,0,0,116,115,1,0,0,0,117,118,1,0,
	0,0,118,116,1,0,0,0,118,119,1,0,0,0,119,28,1,0,0,0,11,0,34,40,42,48,55,
	57,70,80,100,118,1,6,0,0];

	private static __ATN: ATN;
	public static get _ATN(): ATN {
		if (!CohortRuleLexer.__ATN) {
			CohortRuleLexer.__ATN = new ATNDeserializer().deserialize(CohortRuleLexer._serializedATN);
		}

		return CohortRuleLexer.__ATN;
	}


	static DecisionsToDFA = CohortRuleLexer._ATN.decisionToState.map( (ds: DecisionState, index: number) => new DFA(ds, index) );
}