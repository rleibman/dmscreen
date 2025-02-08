#[derive(Debug, PartialEq)] // Add PartialEq to allow assert_eq! in tests
pub enum Exploding {
    None,
    Once,
    Indefinite,
}

#[derive(Debug, PartialEq)]
pub enum KeepDrop {
    KeepHighest(usize),
    KeepLowest(usize),
    DropHighest(usize),
    DropLowest(usize),
    None,
}

#[derive(Debug, PartialEq)] // Needed for assertions in tests
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
