use pest::Parser;
use pest_derive::Parser;
use std::iter::Peekable;
use pest::iterators::{Pair, Pairs};

mod dice;
pub use dice::{DiceRoll, Exploding, KeepDrop};

#[derive(Parser)]
#[grammar = "dice.pest"]
struct DiceParser;

#[derive(Debug)]
pub enum Expr {
    Number(i64),
    DiceRoll(DiceRoll),
    BinaryOp(Box<Expr>, String, Box<Expr>),
    Function(String, Box<Expr>),
}

fn parse_dice_roll(mut pairs: Peekable<Pairs<Rule>>, dice_rolls: &mut Vec<DiceRoll>) -> Expr {
    let num_dice = if let Some(pair) = pairs.next() {
        if pair.as_rule() == Rule::number {
            pair.as_str().parse::<usize>().unwrap()
        } else {
            1
        }
    } else {
        1
    };

    let die_size = match pairs.next().unwrap().as_str() {
        "%" => 100,
        n => n.parse::<usize>().unwrap()
    };

    let mut exploding = Exploding::None;
    let mut keep_drop = KeepDrop::None;
    let modifier = 0;

    while let Some(modifier_pair) = pairs.next() {
        match modifier_pair.as_rule() {
            Rule::modifier => {
                let inner = modifier_pair.into_inner().next().unwrap();
                match inner.as_rule() {
                    Rule::exploding => {
                        exploding = Exploding::Indefinite;
                    }
                    Rule::keep => {
                        let mut inner = inner.into_inner();
                        let kind = inner.next().unwrap().as_str();
                        let num = inner.next()
                            .map(|n| n.as_str().parse::<usize>().unwrap())
                            .unwrap_or(1);
                        keep_drop = match kind {
                            "k" | "kh" => KeepDrop::KeepHighest(num),
                            "kl" => KeepDrop::KeepLowest(num),
                            _ => unreachable!()
                        };
                    }
                    Rule::drop => {
                        let mut inner = inner.into_inner();
                        let kind = inner.next().unwrap().as_str();
                        let num = inner.next()
                            .map(|n| n.as_str().parse::<usize>().unwrap())
                            .unwrap_or(1);
                        keep_drop = match kind {
                            "d" | "dh" => KeepDrop::DropHighest(num),
                            "dl" => KeepDrop::DropLowest(num),
                            _ => unreachable!()
                        };
                    }
                    _ => {}
                }
            }
            _ => {}
        }
    }

    let roll = DiceRoll::new(num_dice, die_size, exploding, keep_drop, modifier);
    dice_rolls.push(roll.clone());
    Expr::DiceRoll(roll)
}

pub fn parse(input: &str) -> Result<(Expr, Vec<DiceRoll>), pest::error::Error<Rule>> {
    let mut dice_rolls = Vec::new();
    let parsed = DiceParser::parse(Rule::expression, input)?
        .next()
        .unwrap();

    fn parse_expr(pair: Pair<Rule>, dice_rolls: &mut Vec<DiceRoll>) -> Expr {
        match pair.as_rule() {
            Rule::expression => {
                let mut pairs = pair.into_inner();
                let first = parse_expr(pairs.next().unwrap(), dice_rolls);

                let mut result = first;
                while let Some(op_pair) = pairs.next() {
                    if op_pair.as_rule() == Rule::operator {
                        let op = op_pair.as_str().to_string();
                        let rhs = parse_expr(pairs.next().unwrap(), dice_rolls);
                        result = Expr::BinaryOp(Box::new(result), op, Box::new(rhs));
                    }
                }
                result
            }
            Rule::term => parse_expr(pair.into_inner().next().unwrap(), dice_rolls),
            Rule::dice_roll => parse_dice_roll(pair.into_inner().peekable(), dice_rolls),
            Rule::number => Expr::Number(pair.as_str().parse::<i64>().unwrap()),
            Rule::function => {
                let mut pairs = pair.into_inner();
                let name = pairs.next().unwrap().as_str().to_string();
                let arg = parse_expr(pairs.next().unwrap(), dice_rolls);
                Expr::Function(name, Box::new(arg))
            }
            _ => unreachable!()
        }
    }

    let expr = parse_expr(parsed.into_inner().next().unwrap(), &mut dice_rolls);
    Ok((expr, dice_rolls))
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_basic_roll() {
        let (_, rolls) = parse("2d20").unwrap();
        assert_eq!(rolls[0], DiceRoll::new(2, 20, Exploding::None, KeepDrop::None, 0));
    }

    #[test]
    fn test_complex_expression() {
        let (_, rolls) = parse("2d20k1 + 1d6!").unwrap();
        assert_eq!(rolls[0], DiceRoll::new(2, 20, Exploding::None, KeepDrop::KeepHighest(1), 0));
        assert_eq!(rolls[1], DiceRoll::new(1, 6, Exploding::Indefinite, KeepDrop::None, 0));
    }
}
