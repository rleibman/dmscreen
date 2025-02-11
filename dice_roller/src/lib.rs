use pest::iterators::{Pair, Pairs};
use pest::Parser;
use pest_derive::Parser;
use std::iter::Peekable;

mod dice;
pub use dice::{DiceRoll, Exploding, KeepDrop};

#[derive(Parser)]
#[grammar = "dice.pest"]
struct DiceParser;

#[derive(Debug, PartialEq, Clone)]
pub enum Expr {
    Number(i64),
    DiceRoll(DiceRoll),
    BinaryOp(Box<Expr>, String, Box<Expr>),
    Function(String, Box<Expr>),
}

fn parse_dice_roll(mut pairs: Peekable<Pairs<Rule>>, dice_rolls: &mut Vec<DiceRoll>) -> Expr {
    let num_dice = if let Some(pair) = pairs.peek() {
        if pair.as_rule() == Rule::dice_number {
            pairs.next().unwrap().as_str().parse::<usize>().unwrap()
        } else {
            1
        }
    } else {
        1
    };

    let die_size = match pairs.next().unwrap().as_str() {
        "%" => 100,
        n => n.parse::<usize>().unwrap(),
    };

    let mut exploding = Exploding::None;
    let mut keep_drop = KeepDrop::None;

    while let Some(modifier_pair) = pairs.next() {
        match modifier_pair.as_rule() {
            Rule::modifier => {
                let inner = modifier_pair.into_inner().next().unwrap();
                match inner.as_rule() {
                    Rule::indefinite => {
                        exploding = Exploding::Indefinite;
                    }
                    Rule::exploding => {
                        exploding = Exploding::Once;
                    }
                    Rule::keep => {
                        let mut inner = inner.into_inner();
                        let kind = inner.next().unwrap();
                        let num = inner
                            .next()
                            .map(|n| n.as_str().parse::<usize>().unwrap())
                            .unwrap_or(1);
                        keep_drop = match kind.as_str() {
                            "k" | "kh" => KeepDrop::KeepHighest(num),
                            "kl" => KeepDrop::KeepLowest(num),
                            _ => panic!("Invalid keep type: {}", kind.as_str()),
                        };
                    }
                    Rule::drop => {
                        let mut inner = inner.into_inner();
                        let kind = inner.next().unwrap();
                        let num = inner
                            .next()
                            .map(|n| n.as_str().parse::<usize>().unwrap())
                            .unwrap_or(1);
                        keep_drop = match kind.as_str() {
                            "d" | "dh" => KeepDrop::DropHighest(num),
                            "dl" => KeepDrop::DropLowest(num),
                            _ => panic!("Invalid drop type: {}", kind.as_str()),
                        };
                    }
                    _ => {}
                }
            }
            _ => {}
        }
    }

    let roll = DiceRoll::new(num_dice, die_size, exploding, keep_drop);
    dice_rolls.push(roll.clone());
    Expr::DiceRoll(roll)
}

pub fn parse(input: &str) -> Result<(Expr, Vec<DiceRoll>), pest::error::Error<Rule>> {
    let mut dice_rolls = Vec::new();
    let parsed = DiceParser::parse(Rule::expression, input)?.next().unwrap();

    fn parse_expr(pair: Pair<Rule>, dice_rolls: &mut Vec<DiceRoll>) -> Expr {
        match pair.as_rule() {
            Rule::expression => parse_expr(pair.into_inner().next().unwrap(), dice_rolls),
            Rule::operation => {
                let mut pairs = pair.into_inner().peekable();
                let mut expr = parse_expr(pairs.next().unwrap(), dice_rolls);

                while let Some(op) = pairs.next() {
                    if op.as_rule() == Rule::operator {
                        let rhs = pairs.next().unwrap();
                        expr = Expr::BinaryOp(
                            Box::new(expr),
                            op.as_str().to_string(),
                            Box::new(parse_expr(rhs, dice_rolls)),
                        );
                    }
                }
                expr
            }
            Rule::term => parse_expr(pair.into_inner().next().unwrap(), dice_rolls),
            Rule::dice_roll => parse_dice_roll(pair.into_inner().peekable(), dice_rolls),
            Rule::term_number => Expr::Number(pair.as_str().parse::<i64>().unwrap()),
            Rule::function => {
                let mut pairs = pair.into_inner();
                let name = pairs.next().unwrap().as_str().to_string();
                let arg = parse_expr(pairs.next().unwrap(), dice_rolls);
                Expr::Function(name, Box::new(arg))
            }
            _ => unreachable!("{:?}", pair.as_rule()),
        }
    }

    let expr = parse_expr(parsed, &mut dice_rolls);
    Ok((expr, dice_rolls))
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_basic_roll() {
        let (_, rolls) = parse("2d20").unwrap();
        assert_eq!(
            rolls[0],
            DiceRoll::new(2, 20, Exploding::None, KeepDrop::None, 0)
        );
    }

    #[test]
    fn test_complex_expression() {
        let (expr, rolls) = parse("2d20k1+1d6!").unwrap();
        println!("Expression: {:?}", expr);
        println!("Rolls: {:?}", rolls);
        assert_eq!(
            rolls[0],
            DiceRoll::new(2, 20, Exploding::None, KeepDrop::KeepHighest(1), 0)
        );
        assert_eq!(
            rolls[1],
            DiceRoll::new(1, 6, Exploding::Once, KeepDrop::None, 0)
        );
    }
}
