#[derive(Debug, PartialEq, Clone)]
pub enum Exploding {
    None,
    Once,
    Indefinite,
}

#[derive(Debug, PartialEq, Clone)]
pub enum KeepDrop {
    KeepHighest(usize),
    KeepLowest(usize),
    DropHighest(usize),
    DropLowest(usize),
    None,
}

#[derive(Debug, PartialEq, Clone)]
pub struct DiceRoll {
    pub num_dice: usize,
    pub die_size: usize,
    pub exploding: Exploding,
    pub keep_drop: KeepDrop,
    pub modifier: isize,
}

impl DiceRoll {
    pub fn new(num_dice: usize, die_size: usize, exploding: Exploding, keep_drop: KeepDrop, modifier: isize) -> Self {
        Self {
            num_dice,
            die_size,
            exploding,
            keep_drop,
            modifier,
        }
    }
}
